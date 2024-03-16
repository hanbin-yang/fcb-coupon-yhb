local cacheKey = KEYS[1]
local realReceiveCount = tonumber(ARGV[1])
local oldMonthCount = tonumber(ARGV[2])
local oldDayCount = tonumber(ARGV[3])
local currReceiveDate = tonumber(ARGV[4])
local oldLastReceiveDate = tonumber(ARGV[5]);
if (redis.call('exists',cacheKey) == 0) then
    return nil
end
local redisLastReceiveDate = tonumber(redis.call('hget', cacheKey, 'lastReceiveDate'))
if (redisLastReceiveDate == currReceiveDate) then
    redis.call("hincrby", cacheKey, 'totalCount', -realReceiveCount)
    redis.call("hset", cacheKey, 'monthCount', oldMonthCount)
    redis.call("hset", cacheKey, 'todayCount', oldDayCount)
    redis.call("hset", cacheKey, 'lastReceiveDate', oldLastReceiveDate)
    return 1;
else
    redis.call("hincrby", cacheKey, 'totalCount', -realReceiveCount)
    redis.call("hincrby", cacheKey, 'monthCount', -realReceiveCount)
    redis.call("hincrby", cacheKey, 'todayCount', -realReceiveCount)
    return 1;
end