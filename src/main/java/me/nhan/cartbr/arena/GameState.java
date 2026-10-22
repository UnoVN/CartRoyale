package me.nhan.cartbr.arena;

public enum GameState {
    WAITING,
    STARTING,
    DRAGON_DROP,
    INGAME,
    DEATHMATCH,
    ENDING,
    RESETTING
}
```

### File 17: `src/main/java/me/nhan/cartbr/arena/PhaseType.java`
```java
package me.nhan.cartbr.arena;

public enum PhaseType {
    DROP,
    PHASE_1,
    PHASE_2,
    PHASE_3,
    DEATHMATCH
}
