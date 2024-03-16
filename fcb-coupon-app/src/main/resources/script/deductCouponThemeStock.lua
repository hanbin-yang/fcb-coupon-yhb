local cacheKey=KEYS[1];
local count=tonumber(ARGV[1]);
local mode=tonumber(ARGV[2]);
if (redis.call('exists',cacheKey)==0 or redis.call('hexists', cacheKey, 'sendedCount') == 0 or redis.call('hexists', cacheKey, 'createdCount') == 0) then
    return nil;
end;
local createdCount=tonumber(redis.call('hget',cacheKey,'createdCount'));
local sendedCount=tonumber(redis.call('hget',cacheKey,'sendedCount'));
local stock=createdCount-sendedCount;
local realCount=tonumber(stock-count);
if(realCount<0 and stock>0) then
    if (mode==0) then
        return tonumber(0);
    elseif (mode==1) then
        redis.call('hincrby',cacheKey,'sendedCount',stock);
        return tonumber(stock);
    end;
elseif(realCount<0 and stock<=0) then
    return tonumber(0);
elseif(realCount>=0) then
    redis.call('hincrby',cacheKey,'sendedCount',count);
    return tonumber(count);
end;
