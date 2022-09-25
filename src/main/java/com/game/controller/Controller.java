package com.game.controller;

import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.service.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rest")
public class Controller {
    private final Service service;

    @Autowired
    public Controller(Service service) {
        this.service = service;
    }

    @GetMapping("/players")
    public List<Player> getPlayersList(@RequestParam(value = "name", required = false) String name,
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
                                       @RequestParam(defaultValue = "ID", value = "order") PlayerOrder order,
                                       @RequestParam(defaultValue = "0", value = "pageNumber") Integer pageNumber,
                                       @RequestParam(defaultValue = "3", value = "pageSize") Integer pageSize
    ) {
        List<Player> resultList = service.getFilteredPlayers(name, title, race, profession,
                after, before, banned, minExperience, maxExperience, minLevel, maxLevel);

        return service.getSortedPlayers(resultList, pageNumber, pageSize, order);
    }

    @GetMapping("/players/count")
    public Integer GetPlayersCount(@RequestParam(value = "name", required = false) String name,
                                   @RequestParam(value = "title", required = false) String title,
                                   @RequestParam(value = "race", required = false) Race race,
                                   @RequestParam(value = "profession", required = false) Profession profession,
                                   @RequestParam(value = "after", required = false) Long after,
                                   @RequestParam(value = "before", required = false) Long before,
                                   @RequestParam(value = "banned", required = false) Boolean banned,
                                   @RequestParam(value = "minExperience", required = false) Integer minExperience,
                                   @RequestParam(value = "maxExperience", required = false) Integer maxExperience,
                                   @RequestParam(value = "minLevel", required = false) Integer minLevel,
                                   @RequestParam(value = "maxLevel", required = false) Integer maxLevel) {
        List<Player> playerList = service.getFilteredPlayers(name, title, race, profession,
                after, before, banned, minExperience, maxExperience, minLevel, maxLevel);

        return playerList.size();
    }

    @PostMapping("/players")
    public ResponseEntity<Player> addNewPlayer(@RequestBody Player player) {
        return new ResponseEntity<>(service.addNewPlayer(player), HttpStatus.OK);
    }

    @GetMapping("/players/{id}")
    public Player getPlayer(@PathVariable(value = "id") String id) {
        return service.getPlayer(id);
    }

    @PostMapping("/players/{id}")
    public Player updatePlayer(
            @PathVariable(value = "id") String id,
            @RequestBody Player player) {

        if (player.getName() == null && player.getTitle() == null
                && player.getProfession() == null && player.getRace() == null
                && player.getBirthday() == null && player.getExperience() == null)

            return service.getPlayer(id);

        return service.updatePlayer(player, id);
    }

    @DeleteMapping("/players/{id}")
    public void deletePlayer(@PathVariable(value = "id") String id) {
        service.deletePlayer(id);
    }
}
