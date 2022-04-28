package com.game.service;

import com.game.controller.PlayerOrder;
import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.repository.PlayerRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PlayerService {

    final PlayerRepo playerRepo;

    @Autowired
    public PlayerService(PlayerRepo userRepo) {
        this.playerRepo = userRepo;
    }


    public List<Player> getPlayers() {
        return playerRepo.findAll();
    }

    public Optional<Player> getPlayerById(Long id) {
        return playerRepo.findById(id);
    }


    public void deletePlayerById(Long id) {
        playerRepo.deleteById(id);
    }

    public boolean existsPlayer(Long id) {
        return playerRepo.existsById(id);
    }

    public Player savePlayer(Player player) {
        return playerRepo.save(player);
    }

    public List<Player> getPlayerList(String name, String title, Race race, Profession profession, Long after, Long before, Boolean banned, Integer minExperience, Integer maxExperience, Integer minLevel, Integer maxLevel, PlayerOrder order, Integer pageNumber, Integer pageSize) {
        List<Player> players = getPlayers(name, title, race, profession, after, before, banned, minExperience, maxExperience, minLevel, maxLevel);

        if (order != null) {
            if (order.equals(PlayerOrder.NAME)) {
                players = players.stream().sorted(Comparator.comparing(Player::getName)).collect(Collectors.toList());
            } else if (order.equals(PlayerOrder.BIRTHDAY)) {
                players = players.stream().sorted(Comparator.comparing(Player::getBirthday)).collect(Collectors.toList());
            } else if (order.equals(PlayerOrder.EXPERIENCE)) {
                players = players.stream().sorted(Comparator.comparing(Player::getExperience)).collect(Collectors.toList());
            } else if (order.equals(PlayerOrder.LEVEL)) {
                players = players.stream().sorted(Comparator.comparing(Player::getLevel)).collect(Collectors.toList());
            }
        } else {
            players = players.stream().sorted(Comparator.comparing(Player::getId)).collect(Collectors.toList());
        }

        if (pageNumber == null) pageNumber = 0;
        if (pageSize == null) pageSize = 3;

        List<Player> result = new ArrayList<>();

        for (int i = pageNumber * pageSize; (i < players.size()) && (i < (pageNumber + 1) * pageSize); i++) {
            result.add(players.get(i));
        }
        return result;
    }

    private List<Player> getPlayers(String name, String title, Race race, Profession profession, Long after, Long before, Boolean banned, Integer minExperience, Integer maxExperience, Integer minLevel, Integer maxLevel) {
        return getPlayers().stream().filter(player -> {
            if (name != null) if (!player.getName().contains(name)) return false;
            if (title != null) if (!player.getTitle().contains(title)) return false;
            if (race != null) if (!(player.getRace().toString().equals(race.toString()))) return false;
            if (profession != null) if (!player.getProfession().equals(profession)) return false;
            if (before != null) if (player.getBirthday().getTime() > before) return false;
            if (after != null) if (player.getBirthday().getTime() < after) return false;
            if (banned != null) if (!(player.getBanned().equals(banned))) return false;
            if (minExperience != null) if (player.getExperience() < minExperience) return false;
            if (maxExperience != null) if (player.getExperience() > maxExperience) return false;
            if (minLevel != null) if (player.getLevel() < minLevel) return false;
            if (maxLevel != null) if (player.getLevel() > maxLevel) return false;
            return true;
        }).collect(Collectors.toList());
    }


    public Integer getPlayerListCount(String name, String title, Race race, Profession profession, Long after, Long before, Boolean banned, Integer minExperience, Integer maxExperience, Integer minLevel, Integer maxLevel) {
        List<Player> players = getPlayers(name, title, race, profession, after, before, banned,
                minExperience, maxExperience, minLevel, maxLevel);

        return players.size();
    }

    public void setLvlAndUntilNextLevel(Player player, Player nPlayer) {
        int lvl = (int) ((Math.sqrt(2500 + 200 * player.getExperience()) - 50) / 100);

        int untilNextLevel = 50 * (lvl + 1) * (lvl + 2) - player.getExperience();

        nPlayer.setLevel(lvl);
        nPlayer.setUntilNextLevel(untilNextLevel);
    }

    public ResponseEntity<Player> getPlayerResponseEntity(Player player, Player playerGetById) {
        if (player.getName() != null) {
            if (!player.getName().isEmpty() && player.getName().length() <= 12) {
                playerGetById.setName(player.getName());
            } else {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }
        if (player.getTitle() != null) {
            if (!player.getTitle().isEmpty() && player.getTitle().length() <= 30) {
                playerGetById.setTitle(player.getTitle());
            } else {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }

        if (player.getRace() != null) {
            playerGetById.setRace(player.getRace());
        }
        if (player.getProfession() != null) {
            playerGetById.setProfession(player.getProfession());
        }
        if (player.getBirthday() != null) {
            if (player.getBirthday().getTime() > 0L) {
                playerGetById.setBirthday(player.getBirthday());
            } else {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }

        if (player.getBirthday() != null) {
            if (LocalDate.parse(new SimpleDateFormat("yyyy-MM-dd")
                    .format(player.getBirthday())).getYear() >= 2000) {
                playerGetById.setBirthday(player.getBirthday());
            } else {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }

        if (player.getBirthday() != null) {
            if (LocalDate.parse(new SimpleDateFormat("yyyy-MM-dd").format(player.getBirthday())).getYear() <= 3000) {
                playerGetById.setBirthday(player.getBirthday());
            } else {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }

        if (player.getExperience() != null) {
            if (player.getExperience() >= 0 && player.getExperience() <= 10_000_000) {
                playerGetById.setExperience(player.getExperience());
            } else {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }

        if (player.getBanned() != null) {
            playerGetById.setBanned(player.getBanned());
        }

        if (player.getExperience() != null) {
            setLvlAndUntilNextLevel(player, playerGetById);
        }
        return null;
    }
}
