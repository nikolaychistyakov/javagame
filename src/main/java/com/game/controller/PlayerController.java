package com.game.controller;

import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.service.PlayerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/rest/players")
public class PlayerController {

    final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }


    @GetMapping
    public ResponseEntity<List<Player>> getPlayers(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "race", required = false) Race race,
            @RequestParam(value = "profession", required = false) Profession profession,
            @RequestParam(value = "after", required = false) Long after,
            @RequestParam(value = "before", required = false) Long before,
            @RequestParam(value = "banned", required = false) Boolean banned,
            @RequestParam(value = "minExperience", required = false) Integer minExperience,
            @RequestParam(value = "maxExperience", required = false) Integer maxExperience,
            @RequestParam(value = "minLevel", required = false) Integer minLevel,
            @RequestParam(value = "maxLevel", required = false) Integer maxLevel,
            @RequestParam(value = "order", required = false) PlayerOrder order,
            @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
            @RequestParam(value = "pageSize", required = false) Integer pageSize
    ) {

        return ResponseEntity.ok(playerService.getPlayerList(name, title, race, profession, after,
                before, banned, minExperience, maxExperience, minLevel, maxLevel, order, pageNumber, pageSize));

    }


    @GetMapping("/count")
    public ResponseEntity<Integer> getPlayersCount(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "race", required = false) Race race,
            @RequestParam(value = "profession", required = false) Profession profession,
            @RequestParam(value = "after", required = false) Long after,
            @RequestParam(value = "before", required = false) Long before,
            @RequestParam(value = "banned", required = false) Boolean banned,
            @RequestParam(value = "minExperience", required = false) Integer minExperience,
            @RequestParam(value = "maxExperience", required = false) Integer maxExperience,
            @RequestParam(value = "minLevel", required = false) Integer minLevel,
            @RequestParam(value = "maxLevel", required = false) Integer maxLevel
    ) {

        return ResponseEntity.ok(playerService.getPlayerListCount(name, title, race, profession, after,
                before, banned, minExperience, maxExperience, minLevel, maxLevel));
    }


    @RequestMapping(
            method = RequestMethod.POST,
            headers = "Accept=application/json")
    public ResponseEntity<Player> createPlayer(@RequestBody Player player) {


        if (player.getName() != null && !player.getName().isEmpty() && player.getName().length() <= 12
                && player.getTitle() != null && !player.getTitle().isEmpty() && player.getTitle().length() <= 30
                && player.getRace() != null
                && player.getProfession() != null
                && player.getBirthday() != null && player.getBirthday().getTime() > 0L
                && LocalDate.parse(new SimpleDateFormat("yyyy-MM-dd").format(player.getBirthday())).getYear() >= 2000
                && LocalDate.parse(new SimpleDateFormat("yyyy-MM-dd").format(player.getBirthday())).getYear() <= 3000
                && player.getExperience() != null && player.getExperience() >= 0 && player.getExperience() <= 10_000_000
        ) {

            Player nPlayer = new Player();
            nPlayer.setName(player.getName());
            nPlayer.setTitle(player.getTitle());
            nPlayer.setRace(player.getRace());
            nPlayer.setProfession(player.getProfession());
            nPlayer.setBirthday(player.getBirthday());
            nPlayer.setBanned(player.getBanned() != null && player.getBanned());
            nPlayer.setExperience(player.getExperience());

            playerService.setLvlAndUntilNextLevel(player, nPlayer);


            return ResponseEntity.ok(playerService.savePlayer(nPlayer));

        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }


    @GetMapping("/{id}")
    public ResponseEntity<Player> getPlayerById(@PathVariable(value = "id") Long id) {
        if (!playerService.existsPlayer(id)) {
            if (id <= 0) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Optional<Player> player = playerService.getPlayerById(id);
        return player.map(ResponseEntity::ok).orElseGet(() -> new ResponseEntity<>(HttpStatus.BAD_REQUEST));

    }

    @PostMapping("/{id}")
    public ResponseEntity<Player> updatePlayer(@PathVariable("id") Long id,
                                               @RequestBody Player player
    ) {

        if (id <= 0) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Optional<Player> playerOptional = playerService.getPlayerById(id);

        if (playerOptional.isPresent()) {

            Player playerGetById = playerOptional.get();

            ResponseEntity<Player> BAD_REQUEST = playerService.getPlayerResponseEntity(player, playerGetById);
            if (BAD_REQUEST != null) return BAD_REQUEST;


            return ResponseEntity.ok(playerService.savePlayer(playerGetById));

        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }


    }

    @RequestMapping(
            value = "/{id}",
            method = RequestMethod.DELETE,
            headers = "Accept=application/json")
    public ResponseEntity<Player> deletePlayer(@PathVariable(value = "id") Long id) {


        if (id <= 0) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }


        if (playerService.existsPlayer(id)) {
            playerService.deletePlayerById(id);

            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

}
