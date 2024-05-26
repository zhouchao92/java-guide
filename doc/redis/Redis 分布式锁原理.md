![image.png](https://cdn.nlark.com/yuque/0/2022/png/25811993/1647073556591-ead256ea-79c5-4259-8789-107a7787b93f.png#averageHue=%230f0d0d&clientId=ue8d1958d-9450-4&from=paste&id=u15cbf16d&originHeight=3034&originWidth=1358&originalType=url&ratio=1&rotation=0&showTitle=false&size=300924&status=done&style=none&taskId=u7a92afa5-bd64-482a-a868-31d8567a742&title=)

1. set nx ex 进行加锁，并设定自动解锁时间（自动解锁时间一定要大于业务的执行时间），key + value （一般为 uuid，保证客户端的唯一性，在解锁时需要验证）
2. 看门狗 watch dog 来进行自动延时加锁（锁的自动续期）
3. lua 脚本原子性操作解锁，并校验解锁对象
```lua
-- 解锁脚本
if redis.call('get',KEYS[1]) == ARGV[1] then 
    return redis.call('del',KEYS[1]) 
else 
    return 0 
end
```
```java
/*
 * redisson 看门狗
 * （1）锁的自动续期，如果业务超长，运行期间自动给锁续上新的30s。不必担心业务时长，锁自动过期被删除
 * （2）加锁的业务只要运行完成，就不会给当前锁续期，即使不手动解锁，锁默认在30s后自动删除。
 */
@GetMapping("/hello")
@ResponseBody
public String hello() {
    // 1、获取一把锁，只要锁的名称一致，就是同一把锁
    RLock lock = redissonClient.getLock("my-lock");

    // 2、加锁，默认加的锁时间是30s
    // lock.lock();   // 阻塞式等待
    lock.lock(30, TimeUnit.SECONDS);    // 30s后自动解锁，自动解锁时间一定要大于业务的执行时间
    // 问题：在锁时间到期后，不会自动续期！
    // 1、如果传递了锁的超时时间，就发送给redis执行脚本，进行占锁，默认超时时间就是指定的时间
    // 2、如果没有指定锁的超时时间，就是用看门狗的默认时间 30s 【lockWatchdogTimeout】
    //    只要占锁成功，就会启动一个定时任务【重新给锁设置过期时间，新的时间就是看门狗的过期时间】
    //    internalLockLeaseTime【看门狗时间】 / 3L = 10s 任务延时->续期
    // 最佳实践
    // lock.lock(30, TimeUnit.SECONDS); 省掉了整个续期操作，手动解锁
    try {
        System.out.println("加锁成功----执行业务--" + Thread.currentThread().getId());
        Thread.sleep(300000);   // 休眠30s，模拟超时业务
    } catch (InterruptedException e) {
        e.printStackTrace();
    } finally {
        // 3、解锁
        // 假设解锁代码没有运行，redisson会不会出现死锁--【不会】
        System.out.println("释放锁--" + Thread.currentThread().getId());
        lock.unlock();
    }
    return "hello";
}


public void lock(){
    // 占分布式锁：去redis占坑
    String uuid = UUID.randomUUID().toString();
    Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", uuid, 30, TimeUnit.SECONDS);
    if (lock) {
        Map<String, List<Catalog2Vo>> dataFromDb;
        try {
            // 设置过期时间
            // redisTemplate.expire("lock", 30, TimeUnit.SECONDS);
            // 加锁成功-->执行业务
            dataFromDb = getDataFromDb();
        } finally {
            // 获取值对比+对比成功删除锁=>原子操作，Lua脚本解锁
            /*
                String lockValue = redisTemplate.opsForValue().get("lock");
                if(uuid.equals(lockValue)) {
                    // 删除自己的锁
                    redisTemplate.delete("lock");   // 删除锁
                }
                 */
            // 删除锁
            String script = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
            redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class),
                                  Arrays.asList("lock"), uuid);
        }
        return dataFromDb;
    } else {
        // 加锁失败-->重试（自旋）
        // 休眠100ms
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            log.error(e.toString());
        }
        return this.getCatalogJsonFromDbWithRedisLock();
    }
}
```
