package nic.ua.poker_estimation.adapters.outbound.persistence;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import nic.ua.poker_estimation.application.ports.outbound.LoadRoomOutPort;
import nic.ua.poker_estimation.application.ports.outbound.SaveRoomOutPort;
import nic.ua.poker_estimation.domain.model.ChatMessage;
import nic.ua.poker_estimation.domain.model.EstimationRound;
import nic.ua.poker_estimation.domain.model.Participant;
import nic.ua.poker_estimation.domain.model.Room;
import nic.ua.poker_estimation.domain.model.Vote;
import nic.ua.poker_estimation.domain.model.VoteValue;
import org.springframework.stereotype.Component;

@Component
@Transactional
public class RoomPersistenceAdapter implements LoadRoomOutPort, SaveRoomOutPort {

    private final RoomRepository roomRepository;
    private final ParticipantRepository participantRepository;
    private final EstimationRoundRepository estimationRoundRepository;
    private final VoteRepository voteRepository;
    private final RoundHistoryRepository roundHistoryRepository;
    private final RoundHistoryVoteRepository roundHistoryVoteRepository;
    private final RoomMessageRepository roomMessageRepository;

    public RoomPersistenceAdapter(
        RoomRepository roomRepository,
        ParticipantRepository participantRepository,
        EstimationRoundRepository estimationRoundRepository,
        VoteRepository voteRepository,
        RoundHistoryRepository roundHistoryRepository,
        RoundHistoryVoteRepository roundHistoryVoteRepository,
        RoomMessageRepository roomMessageRepository
    ) {
        this.roomRepository = roomRepository;
        this.participantRepository = participantRepository;
        this.estimationRoundRepository = estimationRoundRepository;
        this.voteRepository = voteRepository;
        this.roundHistoryRepository = roundHistoryRepository;
        this.roundHistoryVoteRepository = roundHistoryVoteRepository;
        this.roomMessageRepository = roomMessageRepository;
    }

    @Override
    public Optional<Room> load(UUID roomId) {
        return roomRepository.findById(roomId)
            .map(roomEntity -> {
                List<Participant> participants = participantRepository.findAllByRoomIdOrderByIdAsc(roomId).stream()
                    .map(entity -> Participant.create(entity.getId(), entity.getDisplayName(), entity.getRole()))
                    .toList();

                EstimationRound currentRound = estimationRoundRepository.findByRoomId(roomId)
                    .map(this::mapCurrentRound)
                    .orElse(null);

                List<EstimationRound> roundHistory = roundHistoryRepository.findAllByRoomIdOrderByStartedAtAsc(roomId).stream()
                    .map(this::mapHistoryRound)
                    .toList();

                List<ChatMessage> messages = roomMessageRepository.findAllByRoomIdOrderBySentAtAscIdAsc(roomId).stream()
                    .map(entity -> ChatMessage.create(
                        entity.getId(),
                        entity.getParticipantId(),
                        entity.getParticipantDisplayName(),
                        entity.getContent(),
                        entity.getSentAt()
                    ))
                    .toList();

                return new Room(
                    roomEntity.getId(),
                    roomEntity.getName(),
                    roomEntity.getDeckType(),
                    roomEntity.getOwnerParticipantId(),
                    participants,
                    currentRound,
                    roundHistory,
                    messages
                );
            });
    }

    @Override
    public Room save(Room room) {
        roomRepository.save(new RoomEntity(room.id(), room.name(), room.deckType(), room.ownerParticipantId()));

        participantRepository.deleteAllByRoomId(room.id());
        participantRepository.saveAll(room.participants().stream()
            .map(participant -> new ParticipantEntity(participant.id(), room.id(), participant.displayName(), participant.role()))
            .toList());

        estimationRoundRepository.findByRoomId(room.id()).ifPresent(existingRound -> {
            voteRepository.deleteAllByRoundId(existingRound.getId());
            estimationRoundRepository.delete(existingRound);
        });
        if (room.currentRound() != null) {
            EstimationRound round = room.currentRound();
            estimationRoundRepository.save(new EstimationRoundEntity(
                room.id(),
                round.id(),
                round.topic(),
                round.status(),
                round.startedAt(),
                round.expiresAt(),
                round.revealedAt(),
                round.durationSeconds()
            ));
            voteRepository.saveAll(round.votes().stream()
                .map(vote -> new VoteEntity(UUID.randomUUID(), round.id(), vote.participantId(), vote.value().value()))
                .toList());
        }

        roundHistoryRepository.findAllByRoomIdOrderByStartedAtAsc(room.id()).forEach(round -> roundHistoryVoteRepository.deleteAllByRoundId(round.getId()));
        roundHistoryRepository.deleteAllByRoomId(room.id());
        roundHistoryRepository.saveAll(room.roundHistory().stream()
            .map(round -> new RoundHistoryEntity(
                round.id(),
                room.id(),
                round.topic(),
                round.status(),
                round.startedAt(),
                round.expiresAt(),
                round.revealedAt(),
                round.durationSeconds()
            ))
            .toList());
        room.roundHistory().forEach(round -> roundHistoryVoteRepository.saveAll(round.votes().stream()
            .map(vote -> new RoundHistoryVoteEntity(UUID.randomUUID(), round.id(), vote.participantId(), vote.value().value()))
            .toList()));

        roomMessageRepository.deleteAllByRoomId(room.id());
        roomMessageRepository.saveAll(room.messages().stream()
            .map(message -> new RoomMessageEntity(
                message.id(),
                room.id(),
                message.participantId(),
                message.participantDisplayName(),
                message.content(),
                message.sentAt()
            ))
            .toList());

        return room;
    }

    private EstimationRound mapCurrentRound(EstimationRoundEntity entity) {
        return new EstimationRound(
            entity.getId(),
            entity.getTopic(),
            entity.getStatus(),
            entity.getStartedAt(),
            entity.getExpiresAt(),
            entity.getRevealedAt(),
            entity.getDurationSeconds(),
            voteRepository.findAllByRoundIdOrderByParticipantIdAsc(entity.getId()).stream()
                .map(voteEntity -> Vote.create(voteEntity.getParticipantId(), VoteValue.create(voteEntity.getValue())))
                .toList()
        );
    }

    private EstimationRound mapHistoryRound(RoundHistoryEntity entity) {
        return new EstimationRound(
            entity.getId(),
            entity.getTopic(),
            entity.getStatus(),
            entity.getStartedAt(),
            entity.getExpiresAt(),
            entity.getRevealedAt(),
            entity.getDurationSeconds(),
            roundHistoryVoteRepository.findAllByRoundIdOrderByParticipantIdAsc(entity.getId()).stream()
                .map(voteEntity -> Vote.create(voteEntity.getParticipantId(), VoteValue.create(voteEntity.getValue())))
                .toList()
        );
    }
}
