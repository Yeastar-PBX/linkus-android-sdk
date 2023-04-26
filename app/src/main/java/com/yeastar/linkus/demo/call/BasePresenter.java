package com.yeastar.linkus.demo.call;

public abstract class BasePresenter<V> {
    /**
     * 绑定的view
     */
    private V mView;
    /**
     * 绑定view，一般在初始化中调用该方法
     */
    public void attachView(V mView) {
        this.mView = mView;
    }
    /**
     * 断开view，一般在onDestroy中调用
     */
    public void detachView() {
        this.mView = null;
    }
    /**
     * 是否与View建立连接
     * 每次调用业务请求的时候都要出先调用方法检查是否与View建立连接
     */
    public boolean isViewAttached(){
        return mView != null;
    }
    /**
     * 获取连接的view
     */
    public V getView(){
        return mView;
    }
}
