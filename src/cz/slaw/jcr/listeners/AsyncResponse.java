package cz.slaw.jcr.listeners;

public interface AsyncResponse<T> {
 void processFinish(T response);
}
