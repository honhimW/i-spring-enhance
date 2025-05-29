package io.github.honhimw.ddd.jimmer.event;

import org.babyfish.jimmer.sql.runtime.AbstractExecutorProxy;
import org.babyfish.jimmer.sql.runtime.ExecutionPurpose;
import org.babyfish.jimmer.sql.runtime.Executor;
import org.jetbrains.annotations.NotNull;

/**
 * @author hon_him
 * @since 2025-03-21
 */

public class CallbackExecutor extends AbstractExecutorProxy {

    private final Callback callback;

    protected CallbackExecutor(Executor raw, Callback callback) {
        super(raw);
        this.callback = callback;
    }


    @Override
    protected CallbackExecutor recreate(Executor raw) {
        return new CallbackExecutor(raw, callback);
    }

    @Override
    protected Batch createBatch(BatchContext raw) {
        return new Batch(raw) {
        };
    }

    @Override
    public <R> R execute(@NotNull Args<R> args) {
        ExecutionPurpose purpose = args.purpose();
        switch (purpose.getType()) {
            case QUERY -> {
            }
            case UPDATE -> {
            }
            case DELETE -> {
            }
            case LOAD -> {
            }
            case EXPORT -> {
            }
            case MUTATE -> {
            }
            case EVICT -> {
            }
            case COMMAND -> {
            }
        }
//        callback.preCreate();
//        callback.preRemove();
//        callback.preUpdate();
        R result = raw.execute(args);
        callback.postCreate(result);
        callback.postRemove(result);
        callback.postUpdate(result);
        callback.postLoad(result);
        return result;
    }
}
