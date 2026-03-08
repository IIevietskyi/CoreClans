package dev.iievietskyi.coreclans.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import dev.iievietskyi.coreclans.CoreClansPlugin;
import dev.iievietskyi.coreclans.model.BountyContract;

public class BountyService {
    private final CoreClansPlugin plugin;

    public BountyService(CoreClansPlugin plugin) {
        this.plugin = plugin;
    }

    public BountyContract create(String issuerClanId, String targetClanId, double reward) {
        if (!plugin.getCoreConfig().bountyEnabled || issuerClanId == null || targetClanId == null || issuerClanId.equals(targetClanId) || reward <= 0) {
            return null;
        }
        BountyContract contract = new BountyContract();
        contract.id = UUID.randomUUID().toString();
        contract.issuerClanId = issuerClanId;
        contract.targetClanId = targetClanId;
        contract.reward = reward;
        contract.createdAt = System.currentTimeMillis();
        contract.expiresAt = contract.createdAt + plugin.getCoreConfig().bountyDefaultDurationHours * 60L * 60L * 1000L;
        contract.target = 1;
        contract.progress = 0;
        plugin.getStorage().data().bounties.put(contract.id, contract);
        return contract;
    }

    public List<BountyContract> listForTarget(String targetClanId) {
        List<BountyContract> out = new ArrayList<BountyContract>();
        for (BountyContract contract : plugin.getStorage().data().bounties.values()) {
            if (targetClanId.equals(contract.targetClanId) && !contract.completed && contract.expiresAt > System.currentTimeMillis()) {
                out.add(contract);
            }
        }
        return out;
    }

    public BountyContract firstActiveForTarget(String targetClanId) {
        for (BountyContract contract : listForTarget(targetClanId)) {
            return contract;
        }
        return null;
    }

    public void onClanWarWin(String winnerClanId, String loserClanId) {
        for (BountyContract contract : listForTarget(loserClanId)) {
            if (!winnerClanId.equals(contract.issuerClanId)) {
                continue;
            }
            contract.progress++;
            if (contract.progress >= contract.target) {
                contract.completed = true;
                if (plugin.getClanService().getClanById(contract.issuerClanId) != null) {
                    plugin.getClanService().getClanById(contract.issuerClanId).bank += contract.reward;
                }
            }
        }
    }

    public void cleanupExpired() {
        Iterator<BountyContract> iterator = plugin.getStorage().data().bounties.values().iterator();
        long now = System.currentTimeMillis();
        while (iterator.hasNext()) {
            BountyContract contract = iterator.next();
            if (contract == null || contract.expiresAt <= now || contract.completed) {
                iterator.remove();
            }
        }
    }
}
