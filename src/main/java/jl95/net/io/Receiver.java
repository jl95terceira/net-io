package jl95.net.io;

import static jl95.lang.SuperPowers.constant;

import java.time.Duration;

import jl95.lang.variadic.*;
import jl95.util.*;

public interface Receiver<T> extends Closeable {

    record TimeoutInfo(Integer  timeoutsSoFar,
                       Duration timeoutAccum) {}
    interface RecvOptions {

        void    onInputException   (Exception ex);
        void    onHandlingException(Exception ex);
        void    onInputTimeout     (TimeoutInfo timeoutInfo);
        Integer inputRetryTimeoutMs();
        
        class Editable implements RecvOptions {

            public Method1<Exception>   inputExcHandler     = (ex) -> System.out.printf("Exception on reading input: %s%n", ex);
            public Method1<Exception>   handlingExcHandler  = (ex) -> System.out.printf("Exception on handling input: %s%n", ex);
            public Method1<TimeoutInfo> inputTimeoutHandler = (info) ->  {};
            public Function0<Integer>   inputRetryTimeoutMs = constant(100);

            @Override public void    onHandlingException(Exception ex) { handlingExcHandler .accept(ex); }
            @Override public void    onInputException   (Exception ex) { inputExcHandler    .accept(ex); }
            @Override public void    onInputTimeout     (TimeoutInfo info) { inputTimeoutHandler.accept(info); }
            @Override public Integer inputRetryTimeoutMs()          { return inputRetryTimeoutMs.apply(); }
        }
        static RecvOptions defaults() {
            return new RecvOptions.Editable();
        }
    }

    void          recvWhile  (Function1<Boolean, T> incomingCbToContinue,
                              RecvOptions           options);
    UVoidFuture   waitStarted();
    UVoidFuture   stop       ();
    UVoidFuture   waitStopped();
    boolean       isReceiving();

    default void recvWhile(Function1<Boolean, T> incomingCbToContinue) {

        recvWhile(incomingCbToContinue, RecvOptions.defaults());
    }
    default void recv     (Method1<T>            incomingCb,
                           RecvOptions           options) {
        recvWhile(incoming -> {
            incomingCb.accept(incoming);
            return true;
        }, options);
    }
    default void recv     (Method1<T>            incomingCb) {

        recv(incomingCb, RecvOptions.defaults());
    }
    default void recvOnce (Method1<T>            incomingCb,
                           RecvOptions           options) {
        recvWhile(incoming -> {
            incomingCb.accept(incoming);
            return false;
        }, options);
    }
    default void recvOnce (Method1<T>            incomingCb) {
        recvOnce(incomingCb, RecvOptions.defaults());
    }
    default void ensureStopped() {
        try {
            if (!isReceiving()) return;
            stop().get();
        }
        catch (BytesIStreamReceiver.NotReceivingException ex) {
            return;
        }
    }
    default <T2> Receiver<T2> adapted(Function1<T2, T> adapterFunction) {
        return new Receiver<>() {

            @Override public void         recvWhile  (Function1<Boolean, T2> incomingCbToContinue, RecvOptions options) {
                Receiver.this.recvWhile(incoming -> {
                    var adaptedIncoming = adapterFunction.apply(incoming);
                    return incomingCbToContinue.apply(adaptedIncoming);
                }, options);
            }
            @Override public UVoidFuture  waitStarted() {
                return Receiver.this.waitStarted();
            }
            @Override public UVoidFuture  stop       () {
                return Receiver.this.stop();
            }
            @Override public UVoidFuture  waitStopped() {
                return Receiver.this.waitStopped();
            }
            @Override public boolean      isReceiving() {
                return Receiver.this.isReceiving();
            }
            @Override public void         close      () {
                Receiver.this.close();
            }
        };
    }

    /**
     * @deprecated Please use {@link #waitStarted()}
     * @return
     */
    @Deprecated
    default UVoidFuture   recvWaitStarted() {
        return waitStarted();
    }
    /**
     * @deprecated Please use {@link #stop()}
     * @return
     */
    @Deprecated
    default UVoidFuture   recvStop       () {
        return stop();
    }
    /**
     * @deprecated Please use {@link #waitStopped()}
     * @return
     */
    @Deprecated
    default UVoidFuture   recvWaitStopped() {
        return waitStopped();
    }
}
