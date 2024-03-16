local cacheKey = KEYS[1]
local count = tonumber(ARGV[1])
local totalLimit = tonumber(ARGV[2])
local monthLimit = tonumber(ARGV[3])
local dayLimit = tonumber(ARGV[4])
local now = tonumber(ARGV[5]);
local beginOfMonth = tonumber(ARGV[6])
local beginOfDay = tonumber(ARGV[7])
if (redis.call('exists',cacheKey) == 0) then
    return nil
end
local oldTotalCount = tonumber(redis.call('hget', cacheKey, 'totalCount'))
local oldMonthCount = tonumber(redis.call('hget', cacheKey, 'monthCount'))
local oldDayCount = tonumber(redis.call('hget', cacheKey, 'todayCount'))
local oldLastReceiveDate = tonumber(redis.call('hget', cacheKey, 'lastReceiveDate'))

if (oldTotalCount + count > totalLimit) then
    return cjson.encode({["realReceiveCount"]= -9999})
elseif (oldLastReceiveDate < beginOfMonth) then
    redis.call("hincrby", cacheKey, 'totalCount', count)
    redis.call("hset", cacheKey, 'monthCount', count)
    redis.call("hset", cacheKey, 'todayCount', count)
    redis.call("hset", cacheKey, 'lastReceiveDate', now)
    return cjson.encode({["realReceiveCount"]=count, ["oldMonthCount"]=oldMonthCount, ["oldDayCount"] = oldDayCount, ["oldLastReceiveDate"]=oldLastReceiveDate})
elseif (oldLastReceiveDate < beginOfDay) then
    if (oldMonthCount + count > monthLimit) then
        return cjson.encode({["realReceiveCount"]= -999})
    end
    redis.call("hincrby", cacheKey, 'totalCount', count)
    redis.call("hincrby", cacheKey, 'monthCount', count)
    redis.call("hset", cacheKey, 'todayCount', count)
    redis.call("hset", cacheKey, 'lastReceiveDate', now)
    return cjson.encode({["realReceiveCount"]=count, ["oldMonthCount"]=oldMonthCount, ["oldDayCount"] = oldDayCount, ["oldLastReceiveDate"]=oldLastReceiveDate})
elseif (oldDayCount + count > dayLimit) then
    return cjson.encode({["realReceiveCount"]= -99})
else
    redis.call("hincrby", cacheKey, 'totalCount', count)
    redis.call("hincrby", cacheKey, 'monthCount', count)
    redis.call("hincrby", cacheKey, 'todayCount', count)
    redis.call("hset", cacheKey, 'lastReceiveDate', now)
     return cjson.encode({["realReceiveCount"]=count, ["oldMonthCount"]=oldMonthCount, ["oldDayCount"] = oldDayCount, ["oldLastReceiveDate"]=oldLastReceiveDate})
end