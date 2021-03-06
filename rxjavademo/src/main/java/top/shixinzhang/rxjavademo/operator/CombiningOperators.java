/*
 * Copyright (c) 2017. shixinzhang (shixinzhang2016@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package top.shixinzhang.rxjavademo.operator;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

/**
 * Description:
 * <br> 组合型操作符
 * http://reactivex.io/documentation/operators.html
 * <p>
 * <br> Created by shixinzhang on 17/7/19.
 * <p>
 * <br> Email: shixinzhang2016@gmail.com
 * <p>
 * <br> https://about.me/shixinzhang
 */

public class CombiningOperators extends BaseOperators {

    private CombiningOperators() {
        //do some init work
    }

    private volatile static CombiningOperators mInstance = new CombiningOperators();

    public static CombiningOperators getInstance() {
        return mInstance;
    }

    public static void destroyInstance() {
        mInstance = null;
    }

    public static void test() {
        getInstance().testCombiningOperators();
    }

    private void testCombiningOperators() {
//        zip();
//        zipWith();

//        combineLatest();
//        withLatestFrom();

//        join();
//        groupJoin();

//        merge();
//
//        startWith();
//
//        concat();

        switchOnNext();
    }

    /**
     * 喜新厌旧，一发射新的 Observable，就取消订阅之前的
     */
    private void switchOnNext() {
        Observable<Observable<Integer>> observableObservable = Observable.unsafeCreate(new Observable.OnSubscribe<Observable<Integer>>() {
            @Override
            public void call(final Subscriber<? super Observable<Integer>> subscriber) {
                for (int i = 0; i < 5; i++) {
                    subscriber.onNext(Observable.range(1, 10).delay(i, TimeUnit.SECONDS));
                }
                subscriber.onCompleted();
            }
        });

        Observable.switchOnNext(observableObservable)
                .subscribe(this.<Integer>getPrintSubscriber());
    }

    /**
     * 按顺序拼接
     */
    private void concat() {

        Observable<Integer> observableA = Observable.range(0 , 5);
        Observable<Integer> observableB = Observable.range(10, 5);

        Observable.concat(observableB, observableA)
                .subscribe(this.<Integer>getPrintSubscriber());
    }

    /**
     * 先发射前面的
     */
    private void startWith() {
        Observable<Integer> observableA = Observable.range(0 , 5);
        Observable<Integer> observableB = Observable.range(10, 5);

        observableB.startWith(observableA)
                .subscribe(this.<Integer>getPrintSubscriber());
    }

    /**
     * 组合两个 Observable 发出的数据，不保证顺序
     */
    private void merge() {
        Observable<Integer> observableA = Observable.range(0 , 5)   //在另外一个线程处理
                .subscribeOn(Schedulers.io());
        Observable<Integer> observableB = Observable.range(10, 5);

        Observable.merge(observableA, observableB)
                .subscribe(this.<Integer>getPrintSubscriber());
    }

    private void groupJoin() {

        //产生 0 2 4 6 8
        Observable<Long> observableA = Observable.interval(1, TimeUnit.SECONDS)
                .map(new Func1<Long, Long>() {
                    @Override
                    public Long call(final Long aLong) {
                        return aLong * 2;
                    }
                })
                .take(5);

        //产生 0 3 6 9 12
        Observable<Long> observableB = Observable.interval(2, TimeUnit.SECONDS)
                .map(new Func1<Long, Long>() {
                    @Override
                    public Long call(final Long aLong) {
                        return aLong * 3;
                    }
                })
                .take(5);

        observableA.groupJoin(observableB,
                new Func1<Long, Observable<Long>>() {       //定义源 Observable 发射数据的时间窗口
                    @Override
                    public Observable<Long> call(final Long aLong) {
                        System.out.println("A:" + aLong);
                        return Observable.just(aLong).delay(2000, TimeUnit.MILLISECONDS);   //延迟 500 毫秒后发射，即声明周期为 1000毫秒
                    }
                }, new Func1<Long, Observable<Long>>() {    //定义第二个 Observable 发射数据的时间窗口
                    @Override
                    public Observable<Long> call(final Long aLong) {
                        System.out.println("B:" + aLong);
                        return Observable.just(aLong).delay(1000, TimeUnit.MILLISECONDS);
                    }
                }, new Func2<Long, Observable<Long>, Observable<String>>() {
                    @Override
                    public Observable<String> call(final Long itemA, final Observable<Long> longObservable) {
                        return longObservable.map(new Func1<Long, String>() {
                            @Override
                            public String call(final Long itemB) {
                                return "groupJoin result:" + itemA + "/" + itemB;
                            }
                        });
                    }
                })
                .subscribe(new Action1<Observable<String>>() {
                    @Override
                    public void call(final Observable<String> observable) {
                        observable.subscribe(new Action1<String>() {
                            @Override
                            public void call(final String s) {
                                System.out.println("onNext:" + s);
                            }
                        });
                    }
                });
    }

    /**
     * 给两个 Observable 发射的元素各自定义一个时间窗口，在任意一方发射数据时，会和对方时间窗口内的数据进行结合，直到其中一方结束
     */
    private void join() {
        //产生 0 2 4 6 8
        Observable<Long> observableA = Observable.interval(1, TimeUnit.SECONDS)
                .map(new Func1<Long, Long>() {
                    @Override
                    public Long call(final Long aLong) {
                        return aLong * 2;
                    }
                })
                .take(5);

        //产生 0 3 6 9 12
        Observable<Long> observableB = Observable.interval(2, TimeUnit.SECONDS)
                .map(new Func1<Long, Long>() {
                    @Override
                    public Long call(final Long aLong) {
                        return aLong * 3;
                    }
                })
                .take(5);

        observableA.join(observableB,
                new Func1<Long, Observable<Long>>() {       //定义源 Observable 发射数据的时间窗口
                    @Override
                    public Observable<Long> call(final Long aLong) {
                        System.out.println("A:" + aLong);
                        return Observable.just(aLong).delay(2000, TimeUnit.MILLISECONDS);   //延迟 500 毫秒后发射，即声明周期为 1000毫秒
                    }
                }, new Func1<Long, Observable<Long>>() {    //定义第二个 Observable 发射数据的时间窗口
                    @Override
                    public Observable<Long> call(final Long aLong) {
                        System.out.println("B:" + aLong);
                        return Observable.just(aLong).delay(1000, TimeUnit.MILLISECONDS);
                    }
                }, new Func2<Long, Long, String>() {    //组合两个 Observable 发射的数据的函数
                    @Override
                    public String call(final Long aLong, final Long aLong2) {
                        return "join result:" + aLong + "/" + aLong2;
                    }
                })
                .subscribe(this.<String>getPrintSubscriber());
    }

    /***
     * 将 A 发射的数据与 B 之前发射最新的数据结合，进行函数操作
     */
    private void combineLatest() {
        Observable<Long> observableA = Observable.interval(3, TimeUnit.SECONDS);
        Observable<Long> observableB = Observable.interval(2, TimeUnit.SECONDS);

        Observable
                .combineLatest(observableA, observableB, new Func2<Long, Long, String>() {
                    @Override
                    public String call(final Long itemA, final Long itemB) {
                        return "combine result: " + itemA + "/" + itemB;
                    }
                })
                .subscribe(this.<String>getPrintSubscriber());
    }

    private void withLatestFrom() {
        Observable<Long> observableA = Observable.interval(3, TimeUnit.SECONDS);
        Observable<Long> observableB = Observable.interval(2, TimeUnit.SECONDS);

        observableB.withLatestFrom(observableA, new Func2<Long, Long, String>() {
            @Override
            public String call(final Long itemA, final Long itemB) {
                return "withLatestFrom: " + itemA + "/" + itemB;
            }
        }).subscribe(this.<String>getPrintSubscriber());
    }

    /**
     * 非静态，可以和当前 Observable 组合
     */
    private void zipWith() {
        Observable<String> observableA = Observable.just("A", "B", "C", "d", "E");
        Observable
                .just(1, 2, 3, 4)
                .zipWith(observableA, new Func2<Integer, String, String>() {
                    @Override
                    public String call(final Integer integer, final String s) {
                        return integer + ", " + s;
                    }
                })
                .subscribe(this.<String>getPrintSubscriber());
    }

    /**
     * 将多个 Observable 发射的结果严格按顺序组合起来
     */
    private void zip() {
        Observable<String> observableA = Observable.just("A", "B", "C", "d", "E");
        Observable<Integer> observableB = Observable.just(1, 2, 3, 4);

        Observable
                .zip(observableA, observableB, new Func2<String, Integer, String>() {
                    @Override
                    public String call(final String s, final Integer integer) {
                        return s + "_" + integer;
                    }
                })
                .subscribe(this.<String>getPrintSubscriber());
    }
}
