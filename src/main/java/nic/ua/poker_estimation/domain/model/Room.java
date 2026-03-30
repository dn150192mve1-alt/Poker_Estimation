package nic.ua.poker_estimation.domain.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import nic.ua.poker_estimation.common.service.ConflictException;
import nic.ua.poker_estimation.common.service.ValidationException;

public record Room(
    UUID id,
    String name,
    DeckType deckType,
    UUID ownerParticipantId,
    List<Participant> participants,
    EstimationRound currentRound,
    List<EstimationRound> roundHistory,
    List<ChatMessage> messages
) {

    public Room {
        participants = List.copyOf(participants);
        roundHistory = List.copyOf(roundHistory);
        messages = List.copyOf(messages);
    }

    public static Room create(UUID id, String name, DeckType deckType, UUID ownerParticipantId, String ownerDisplayName) {
        if (id == null) {
            throw new ValidationException("Room id must not be null");
        }
        if (name == null || name.isBlank()) {
            throw new ValidationException("Room name must not be blank");
        }
        if (deckType == null) {
            throw new ValidationException("Room deckType must not be null");
        }
        Participant owner = Participant.createOwner(ownerParticipantId, ownerDisplayName);
        return new Room(id, name.trim(), deckType, owner.id(), List.of(owner), null, List.of(), List.of());
    }

    public JoinParticipantResult joinParticipant(UUID participantId, String displayName) {
        ensureUniqueParticipantName(displayName);
        Participant participant = Participant.createPlayer(participantId, displayName);
        List<Participant> updatedParticipants = new ArrayList<>(participants);
        updatedParticipants.add(participant);
        updatedParticipants.sort(Comparator.comparing(Participant::id));
        return new JoinParticipantResult(new Room(id, name, deckType, ownerParticipantId, updatedParticipants, currentRound, roundHistory, messages), participant);
    }

    public Room startRound(String topic, long durationSeconds, Instant now) {
        Room normalizedRoom = closeRoundIfExpired(now);
        if (normalizedRoom.currentRound != null && normalizedRoom.currentRound.status() == RoundStatus.ACTIVE) {
            throw new ConflictException("Active estimation round already exists for room " + id);
        }
        return new Room(
            normalizedRoom.id,
            normalizedRoom.name,
            normalizedRoom.deckType,
            normalizedRoom.ownerParticipantId,
            normalizedRoom.participants,
            EstimationRound.start(topic, durationSeconds, now),
            normalizedRoom.historyWithCurrentArchived(),
            normalizedRoom.messages
        );
    }

    public Room restartRound(Instant now) {
        Room normalizedRoom = closeRoundIfExpired(now);
        if (normalizedRoom.currentRound == null) {
            throw new ConflictException("There is no round to restart for room " + id);
        }
        if (normalizedRoom.currentRound.status() == RoundStatus.ACTIVE) {
            throw new ConflictException("Cannot restart active estimation round for room " + id);
        }
        EstimationRound previousRound = normalizedRoom.currentRound;
        return new Room(
            normalizedRoom.id,
            normalizedRoom.name,
            normalizedRoom.deckType,
            normalizedRoom.ownerParticipantId,
            normalizedRoom.participants,
            EstimationRound.start(previousRound.topic(), previousRound.durationSeconds(), now),
            normalizedRoom.historyWithCurrentArchived(),
            normalizedRoom.messages
        );
    }

    public Room submitVote(UUID participantId, String rawValue, Instant now) {
        ensureParticipantExists(participantId);
        if (currentRound == null || currentRound.status() != RoundStatus.ACTIVE) {
            throw new ConflictException("There is no active estimation round for room " + id);
        }
        VoteValue voteValue = deckType.createVoteValue(rawValue);
        return new Room(id, name, deckType, ownerParticipantId, participants, currentRound.submitVote(participantId, voteValue, now), roundHistory, messages);
    }

    public Room revealEstimations(Instant now) {
        if (currentRound == null) {
            throw new ConflictException("There is no round to reveal for room " + id);
        }
        return new Room(id, name, deckType, ownerParticipantId, participants, currentRound.closeIfExpired(now).reveal(now), roundHistory, messages);
    }

    public Room closeRoundIfExpired(Instant now) {
        if (currentRound == null) {
            return this;
        }
        return new Room(id, name, deckType, ownerParticipantId, participants, currentRound.closeIfExpired(now), roundHistory, messages);
    }

    public Room postMessage(UUID participantId, String content, Instant now) {
        Participant participant = findParticipant(participantId);
        ChatMessage message = ChatMessage.create(UUID.randomUUID(), participantId, participant.displayName(), content, now);
        List<ChatMessage> updatedMessages = new ArrayList<>(messages);
        updatedMessages.add(message);
        updatedMessages.sort(Comparator.comparing(ChatMessage::sentAt).thenComparing(ChatMessage::id));
        return new Room(id, name, deckType, ownerParticipantId, participants, currentRound, roundHistory, updatedMessages);
    }

    private List<EstimationRound> historyWithCurrentArchived() {
        List<EstimationRound> updatedHistory = new ArrayList<>(roundHistory);
        if (currentRound != null) {
            updatedHistory.add(currentRound);
            updatedHistory.sort(Comparator.comparing(EstimationRound::startedAt).thenComparing(EstimationRound::id));
        }
        return updatedHistory;
    }

    private Participant findParticipant(UUID participantId) {
        return participants.stream()
            .filter(participant -> participant.id().equals(participantId))
            .findFirst()
            .orElseThrow(() -> new ConflictException("Participant is not part of room " + id + ": " + participantId));
    }

    private void ensureParticipantExists(UUID participantId) {
        findParticipant(participantId);
    }

    private void ensureUniqueParticipantName(String displayName) {
        String normalizedDisplayName = normalize(displayName);
        boolean nameExists = participants.stream()
            .map(Participant::displayName)
            .map(Room::normalize)
            .anyMatch(normalizedDisplayName::equals);
        if (nameExists) {
            throw new ConflictException("Participant name already joined room: " + displayName);
        }
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

    public record JoinParticipantResult(Room room, Participant participant) {
    }
}
