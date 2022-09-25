package com.game.service;

import com.game.controller.PlayerOrder;
import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import java.util.*;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
@Transactional
public class Service {
    private final PlayerRepository playerRepository;

    public List<Player> getFilteredPlayers(String name, String title, Race race, Profession profession,
                                           Long after, Long before, Boolean banned, Integer minExperience,
                                           Integer maxExperience, Integer minLevel, Integer maxLevel) {
        List<Player> result = new ArrayList<>();
        final Date afterDate = after == null ? null : new Date(after);
        final Date beforeDate = before == null ? null : new Date(before);

        playerRepository.findAll().forEach(player -> {
            if (name!=null && !player.getName().contains(name)) return;
            if (title!=null && !player.getTitle().contains(title)) return;
            if (race != null && player.getRace() != race) return;
            if (profession != null && player.getProfession() != profession) return;
            if (after != null && player.getBirthday().before(afterDate)) return;
            if (before != null && player.getBirthday().after(beforeDate)) return;
            if (banned != null && player.getBanned() != banned) return;
            if (minExperience != null && player.getExperience().compareTo(minExperience) < 0) return;
            if (maxExperience != null && player.getExperience().compareTo(maxExperience) > 0) return;
            if (minLevel != null && player.getLevel().compareTo(minLevel) < 0) return;
            if (maxLevel != null && player.getLevel().compareTo(maxLevel) > 0) return;
            result.add(player);
        });
        return result;
    }
    public List<Player> getSortedPlayers(List<Player> allPlayers, Integer page, Integer pageSize, PlayerOrder order) {
        int pageNum = page + 1;
        int count = pageSize;
        List<Player> sortedPlayers = new ArrayList<>();
        if (order.equals(PlayerOrder.NAME)) {
            allPlayers.sort(Comparator.comparing(Player::getName));
        } else if (order.equals(PlayerOrder.EXPERIENCE))
            allPlayers.sort(Comparator.comparing(Player::getExperience));
        else if (order.equals(PlayerOrder.BIRTHDAY))
            allPlayers.sort(Comparator.comparing(Player::getBirthday));
        for (int i = pageNum * count - (count - 1) - 1; i < count * pageNum && i < allPlayers.size(); i++) {
            sortedPlayers.add(allPlayers.get(i));
        }
        return sortedPlayers;
    }

    public Player addNewPlayer(Player player) {
        if (player.getName() == null || player.getName().isBlank() || player.getTitle() == null || player.getRace() == null
                || player.getProfession() == null || player.getBirthday() == null || player.getExperience() == null
                || player.getTitle().length() > 30 || player.getName().length() > 12 || player.getName().equals("")
                || player.getExperience() < 0 || player.getExperience() > 10000000 || player.getBirthday().getTime() < 0
                || player.getBirthday().before(new Date(946684800000L))
                || player.getBirthday().after(new Date(32503680000000L))
        ) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        player.setLevel((int) (Math.sqrt((double) 2500 + 200 * player.getExperience()) - 50) / 100);
        player.setUntilNextLevel(50 * (player.getLevel() + 1) * (player.getLevel() + 2) - player.getExperience());

        return playerRepository.save(player);
    }

    public Player getPlayer(String id) {
        Long newId;
        try {
            newId = Long.parseLong(id);
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        if (!(newId > 0))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        if (playerRepository.existsById(newId)) {
            return playerRepository.findById(newId).get();
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    public Player updatePlayer(Player newPlayer, String id) {
        Player updatePlayer = getPlayer(id);
        updatePlayer.setName(newPlayer.getName() != null ? newPlayer.getName() : updatePlayer.getName());
        updatePlayer.setTitle(newPlayer.getTitle() != null ? newPlayer.getTitle() : updatePlayer.getTitle());
        updatePlayer.setRace(newPlayer.getRace() != null ? newPlayer.getRace() : updatePlayer.getRace());
        updatePlayer.setProfession(newPlayer.getProfession() != null ? newPlayer.getProfession() : updatePlayer.getProfession());
        updatePlayer.setBanned(newPlayer.getBanned() != null ? newPlayer.getBanned() : updatePlayer.getBanned());
        if (newPlayer.getExperience() != null) {
            if (isValidExperience(newPlayer.getExperience())) {
                updatePlayer.setExperience(newPlayer.getExperience());
            } else throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        if (newPlayer.getBirthday() != null) {
            if (isValidDate(newPlayer.getBirthday())) {
                updatePlayer.setBirthday(newPlayer.getBirthday());
            } else throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        updatePlayer.setLevel((int) ((Math.sqrt(2500 + 200 * updatePlayer.getExperience()) - 50) / 100));
        updatePlayer.setUntilNextLevel(50 * (updatePlayer.getLevel() + 1) * (updatePlayer.getLevel() + 2) - updatePlayer.getExperience());

        return playerRepository.save(updatePlayer);
    }

    public void deletePlayer(String id) {
        try {
            Long newId = Long.parseLong(id);
            if (newId <= 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            playerRepository.delete(getPlayer(String.valueOf(newId)));
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    private boolean isValidExperience(Integer experience) {
        return experience >= 0 && experience <= 10000000;
    }

    private boolean isValidDate(Date date) {
        if (date == null) {
            return false;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.set(1999, 11, 31);
        Date after = calendar.getTime();
        calendar.set(3000, 11, 31);
        Date before = calendar.getTime();

        return (date.before(before) && date.after(after));
    }
}
