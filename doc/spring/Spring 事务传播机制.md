```java
public enum Propagation {
    // 如果当前不存在事务，开启新一个事务；如果当前存在事务，则加入当前事务
    REQUIRED(0),
    // 如果当前存在事务则加入当前事务，如果不存在则以非事务的方式运行
    SUPPORTS(1),
    // 强制在事务内执行，如果当前存在事务则加入当前事务，如果当前不存在事务则抛出异常
    MANDATORY(2),
    // 无论是否存在事务，都会开启一个新的事务执行
    REQUIRES_NEW(3),
    // 以非事务的方式执行，如果当前存在事务则挂起
    NOT_SUPPORTED(4),
    // 以非事务的方式执行，如果当前存在事务则抛出异常
    NEVER(5),
    // 如果当前存在事务，嵌套事务的方式执行，如果不存在事务则开启一个新事务
    NESTED(6);

    private final int value;

    private Propagation(int value) {
        this.value = value;
    }

    public int value() {
        return this.value;
    }
}
```