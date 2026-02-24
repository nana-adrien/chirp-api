local key = KEYS[1]
local max_requests = tonumber(ARGV[1])
local ttl = tonumber(ARGV[2])

local current = redis.call('GET', key)
if current == false then
    redis.call('SET',key,1,'EX',ttl)
    return {1,ttl}
else
    local remaining_ttl = redis.call('TTL', key)
    local count = tonumber(current)
    if count < max_requests then
        local new_count = redis.call('INCR', key)
        return {new_count, remaining_ttl}
    else
        return {count + 1,remaining_ttl}

    end
end