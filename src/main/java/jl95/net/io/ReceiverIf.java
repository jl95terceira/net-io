package jl95.net.io;

import static jl95.lang.SuperPowers.constant;

import java.io.InputStream;
import java.time.Duration;

import jl95.lang.*;
import jl95.lang.variadic.*;
import jl95.util.*;

public interface ReceiverIf<T> {

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

    void          recvWhile      (Function1<Boolean, T> incomingCbToContinue,
                                  RecvOptions           options);
    UVoidFuture   recvWaitStarted();
    UVoidFuture   recvStop       ();
    Boolean       isReceiving    ();
    InputStream   getInputStream ();

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
            recvStop().get();
        }
        catch (Receiver.NotReceivingException ex) {
            return;
        }
    }
    default <T2> ReceiverIf<T2> adaptedReceiver(Function1<T2, T> adapterFunction) {
        return new ReceiverIf<>() {

            @Override public void         recvWhile      (Function1<Boolean, T2> incomingCbToContinue, RecvOptions options) {
                ReceiverIf.this.recvWhile(incoming -> {
                    var adaptedIncoming = adapterFunction.apply(incoming);
                    return incomingCbToContinue.apply(adaptedIncoming);
                }, options);
            }
            @Override public UVoidFuture  recvWaitStarted() {
                return ReceiverIf.this.recvWaitStarted();
            }
            @Override public UVoidFuture  recvStop       () {
                return ReceiverIf.this.recvStop();
            }
            @Override public Boolean      isReceiving    () {
                return ReceiverIf.this.isReceiving();
            }
            @Override public InputStream  getInputStream () {
                return ReceiverIf.this.getInputStream();
            }
        };
    }
}
