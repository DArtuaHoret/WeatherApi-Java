package org.example.services;

import org.example.models.DataPogoda;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;
import java.time.Duration;

public class ServiceCache {
    private final JedisPool jedisPool;
    private final Gson gson;
    private final Type dataPogodaListType;

    public ServiceCache(Gson gson) {
        this.gson = gson;
        this.dataPogodaListType = new TypeToken<List<DataPogoda>>() {}.getType();

        // Configure connection pool
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(128);
        poolConfig.setMaxIdle(32);
        poolConfig.setMinIdle(4);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);

        // Create JedisPool with proper constructor
        this.jedisPool = new JedisPool(
                poolConfig,
                "localhost",
                6379
        );
    }

    public void zapiszDanePogodowe(String klucz, List<DataPogoda> dane, boolean isHistorical) {
        try (Jedis jedis = jedisPool.getResource()) {
            String json = gson.toJson(dane, dataPogodaListType);
            System.out.println("Попытка записи в Redis. Ключ: " + klucz);

            jedis.set(klucz, json);
            System.out.println("Данные успешно записаны в Redis");

            if (!isHistorical) {
                jedis.expire(klucz, 3600);
                System.out.println("TTL установлен на 1 час");
            }
        } catch (Exception e) {
            System.err.println("Błąd zapisu cache: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<DataPogoda> pobierzDanePogodowe(String klucz) {
        try (Jedis jedis = jedisPool.getResource()) {
            System.out.println("Попытка чтения из Redis. Ключ: " + klucz);
            String json = jedis.get(klucz);

            if (json == null) {
                System.out.println("Данные не найдены в кеше для ключа: " + klucz);
                return null;
            }

            System.out.println("Успешно получены данные из Redis");
            return gson.fromJson(json, dataPogodaListType);
        } catch (Exception e) {
            System.err.println("Ошибка при чтении из кеша: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}