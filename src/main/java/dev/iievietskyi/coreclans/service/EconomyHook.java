package dev.iievietskyi.coreclans.service;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

@SuppressWarnings({"rawtypes", "unchecked"})
public class EconomyHook {
    private final JavaPlugin plugin;

    private Object provider;

    private Method hasOfflineMethod;
    private Method withdrawOfflineMethod;
    private Method depositOfflineMethod;
    private Method getBalanceOfflineMethod;

    private Method hasNameMethod;
    private Method withdrawNameMethod;
    private Method depositNameMethod;
    private Method getBalanceNameMethod;

    private Method responseSuccessMethod;
    private Field responseSuccessField;

    public EconomyHook(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean setup() {
        this.provider = null;
        this.hasOfflineMethod = null;
        this.withdrawOfflineMethod = null;
        this.depositOfflineMethod = null;
        this.getBalanceOfflineMethod = null;
        this.hasNameMethod = null;
        this.withdrawNameMethod = null;
        this.depositNameMethod = null;
        this.getBalanceNameMethod = null;
        this.responseSuccessMethod = null;
        this.responseSuccessField = null;

        Plugin vault = Bukkit.getPluginManager().getPlugin("Vault");
        if (vault == null || !vault.isEnabled()) {
            return false;
        }

        try {
            Class<?> economyClass = Class.forName("net.milkbowl.vault.economy.Economy");
            RegisteredServiceProvider registration = Bukkit.getServicesManager().getRegistration((Class) economyClass);
            if (registration == null) {
                return false;
            }

            Object found = registration.getProvider();
            if (found == null) {
                return false;
            }

            this.provider = found;

            this.hasOfflineMethod = findMethod(found.getClass(), "has", OfflinePlayer.class, double.class);
            this.withdrawOfflineMethod = findMethod(found.getClass(), "withdrawPlayer", OfflinePlayer.class, double.class);
            this.depositOfflineMethod = findMethod(found.getClass(), "depositPlayer", OfflinePlayer.class, double.class);
            this.getBalanceOfflineMethod = findMethod(found.getClass(), "getBalance", OfflinePlayer.class);

            this.hasNameMethod = findMethod(found.getClass(), "has", String.class, double.class);
            this.withdrawNameMethod = findMethod(found.getClass(), "withdrawPlayer", String.class, double.class);
            this.depositNameMethod = findMethod(found.getClass(), "depositPlayer", String.class, double.class);
            this.getBalanceNameMethod = findMethod(found.getClass(), "getBalance", String.class);

            Class<?> responseClass = Class.forName("net.milkbowl.vault.economy.EconomyResponse");
            this.responseSuccessMethod = findMethod(responseClass, "transactionSuccess");
            try {
                this.responseSuccessField = responseClass.getField("transactionSuccess");
            } catch (Exception ignored) {
                this.responseSuccessField = null;
            }

            return true;
        } catch (Throwable throwable) {
            plugin.getLogger().warning("Vault economy hook failed: " + throwable.getMessage());
            this.provider = null;
            return false;
        }
    }

    public boolean isAvailable() {
        return provider != null;
    }

    public double getBalance(Player player) {
        if (player == null || !isAvailable()) {
            return 0.0D;
        }

        try {
            Object result;
            if (getBalanceOfflineMethod != null) {
                result = getBalanceOfflineMethod.invoke(provider, player);
            } else if (getBalanceNameMethod != null) {
                result = getBalanceNameMethod.invoke(provider, player.getName());
            } else {
                return 0.0D;
            }
            return toDouble(result);
        } catch (Throwable throwable) {
            return 0.0D;
        }
    }

    public boolean has(Player player, double amount) {
        if (player == null || amount <= 0.0D) {
            return true;
        }
        if (!isAvailable()) {
            return false;
        }

        try {
            Object result;
            if (hasOfflineMethod != null) {
                result = hasOfflineMethod.invoke(provider, player, amount);
                if (result instanceof Boolean) {
                    return ((Boolean) result).booleanValue();
                }
            }
            if (hasNameMethod != null) {
                result = hasNameMethod.invoke(provider, player.getName(), amount);
                if (result instanceof Boolean) {
                    return ((Boolean) result).booleanValue();
                }
            }
        } catch (Throwable ignored) {
        }

        return getBalance(player) >= amount;
    }

    public boolean withdraw(Player player, double amount) {
        if (player == null || amount <= 0.0D) {
            return true;
        }
        if (!isAvailable()) {
            return false;
        }

        try {
            Object result;
            if (withdrawOfflineMethod != null) {
                result = withdrawOfflineMethod.invoke(provider, player, amount);
                return responseSucceeded(result);
            }
            if (withdrawNameMethod != null) {
                result = withdrawNameMethod.invoke(provider, player.getName(), amount);
                return responseSucceeded(result);
            }
        } catch (Throwable ignored) {
        }
        return false;
    }

    public boolean deposit(Player player, double amount) {
        if (player == null || amount <= 0.0D) {
            return true;
        }
        if (!isAvailable()) {
            return false;
        }

        try {
            Object result;
            if (depositOfflineMethod != null) {
                result = depositOfflineMethod.invoke(provider, player, amount);
                return responseSucceeded(result);
            }
            if (depositNameMethod != null) {
                result = depositNameMethod.invoke(provider, player.getName(), amount);
                return responseSucceeded(result);
            }
        } catch (Throwable ignored) {
        }
        return false;
    }

    private boolean responseSucceeded(Object response) {
        if (response == null) {
            return false;
        }
        if (response instanceof Boolean) {
            return ((Boolean) response).booleanValue();
        }
        try {
            if (responseSuccessMethod != null) {
                Object ok = responseSuccessMethod.invoke(response);
                if (ok instanceof Boolean) {
                    return ((Boolean) ok).booleanValue();
                }
            }
        } catch (Throwable ignored) {
        }
        try {
            if (responseSuccessField != null) {
                return responseSuccessField.getBoolean(response);
            }
        } catch (Throwable ignored) {
        }
        return false;
    }

    private double toDouble(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (Exception ex) {
            return 0.0D;
        }
    }

    private Method findMethod(Class<?> type, String name, Class<?>... params) {
        try {
            Method method = type.getMethod(name, params);
            method.setAccessible(true);
            return method;
        } catch (Exception ignored) {
            return null;
        }
    }
}