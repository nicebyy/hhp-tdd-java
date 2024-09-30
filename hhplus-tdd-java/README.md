# 동시성 제어 방식 분석

## 해결 방식

1. ConcurrentHashMap<Long, ReentrantLock> lockMap 을 이용하여 사용자 ID에 대해 Lock 을 저장함으로써 동시성 문제중 하나인 갱신손실 예방과 공정성을 중점으로 구현하였습니다.

```java
public UserPoint chargeUserPoint(long id, long amount){
    ReentrantLock lock = getLock(id);
    lock.lock();
    try{
        // 포인트 충전 로직
    } finally {
        lock.unlock();
    }
}

```

- syncronized 는 왜 안 되는걸까 .. ?

  → syncronized 는 메서드 자체에 lock 을 걸어버리기 때문에 해당 스레드에 접근하는 다른 유저들의 스레드들도 같이 대기를 해버리는 매우 안 좋은 상황이 발생하기 때문에 UserID 를 공유자원으로 취급하여 각 다른 유저들의 스레드가 접근해도 ID 가 다르면 스레드가 동작하도록 병렬로 처리가 가능해짐.

- Atomic 타입과 ConcurrentHashMap 모두 CAS 알고리즘에 기반하고 있음
    - (CAS : Compare And Swap 방식으로, non-blocking 기반의 동기화 문제를 해결하는 방식)
    - 기존 값과 변경할 값을 전달하고 변경이 일어날 때 메모리를 조회하여 값이 같으면 변경, 그렇지 않으면 반영하지 않는 방법으로 동작.

## 한계점

- LockMap 의 무한정 커지면 .. ? → Heap 이 넘치거나 메모리누수가 발생할 수 있음. WeakReference 사용을  고려해 보는것도 좋음
- 분산환경을 고려한다면 해당 방식으로 해결을 못함 → 메세지큐 방식이나 Redis 등을 이용하여 해결해야한다.