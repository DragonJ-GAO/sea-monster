package me.add1.network;

import me.add1.common.PriorityRunnable;


public abstract interface HttpHandler {
    public <T> int executeRequest(AbstractHttpRequest<T> request);

    public void cancelRequest(AbstractHttpRequest<?> request);

    public void cancelRequest();


    public abstract class PriorityRequestRunnable<T> extends PriorityRunnable {
        protected AbstractHttpRequest<T> request;


        public PriorityRequestRunnable(AbstractHttpRequest<T> request) {
            super(request.getPriority());
            this.request = request;
        }

        public AbstractHttpRequest<T> getRequest() {
            return request;
        }
    }
}
