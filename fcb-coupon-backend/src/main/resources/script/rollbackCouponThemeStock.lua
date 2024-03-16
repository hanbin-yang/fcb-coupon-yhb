local cacheKey=KEYS[1];
local count=tonumber(ARGV[1]);
if (redis.call('exists',cacheKey) == 0 or redis.call('hexists', cacheKey, 'sendedCount') == 0) then
return nil;
end;
local sendedCount=tonumber(redis.call('hget', cacheKey, 'sendedCount'));
if(sendedCount == 0) then
return 0
elseif(sendedCount < 0) then
redis.call('hset', cacheKey, 'sendedCount', 0);
return 0
elseif(sendedCount < count) then
redis.call('hset', cacheKey, 'sendedCount', 0);
return sendedCount
end
redis.call('hincrby', cacheKey, 'sendedCount', -count);
return count;
