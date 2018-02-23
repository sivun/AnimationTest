# Android帧动画优化
## Useage

    /**
     * 将帧动画资源id以字符串数组形式写到values/test_anima.xml中,
     * 注意：test_anima中分为loading_anim_res(动画资源)和loading_anim_duration(每帧时长)两部分，
     * 两者个数不同时，则用少的一个
     */

     FramesSequenceAnimation  animation = FramesSequenceAnimation.createAnima(imageView,
                    R.array.loading_anim_res, R.array.loading_anim_duration, true);
    animation.start();//动画开始    
    animation.stop();//动画结束

## 未来优化方向
   - 1.动画资源可以加载使用animation-list原始帧动画
   - 2.提供动画重复模式和动画行为监听器（开始、结束、帧、重开始）
