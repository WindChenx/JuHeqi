// IMediaService.aidl
package com.wind.juheqi;

// Declare any non-default types here with import statements

interface IMediaService {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);

            	    /**
            	     * 根据位置打开音乐
            	     * @param position
            	     */
            	   void openAudio(int position);

            	    /**
            	     * 播放音乐
            	     */
            	   void start();

            	    /**
            	     * 暂停音乐
            	     */
            	   void pause();

            	    /**
            	     * 下一首
            	     */
            	    void next();


            	    /**
            	     * 上一首
            	     */
            	    void pre();

            	    /**
            	     * 得到播放模式
            	     * @return
            	     */
            	    int getPlaymode();

            	    /**
            	     * 设置播放模式
            	     * @param playmode
            	     */
            	    void setPlaymode(int playmode);

            	    /**
            	     * 得到当前播放进度
            	     * @return
            	     */
            	    int getCurrentPosition();

            	    /**
            	     * 得到当前的总时长
            	     * @return
            	     */
            	    int getDuration();


            	    /**
            	     * 得到歌曲的名称
            	     * @return
            	     */
            	    String getName();


            	    /**
            	     * 得到演唱者
            	     * @return
            	     */
            	    String getArtist();

            	    /**
            	     * 音频的拖动
            	     * @param seekto
            	     */
            	    void seekTo(int seekto);

            	    boolean isPlaying();
            	    void notifyChange(String action);
            	    void startOnline();

}
