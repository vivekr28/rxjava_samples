package org.rxjava.samples;


import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * This is a sample program to read a file using ObservableSource and have 10 retries in case of file read fails
 */
public class FileReadWithObservable {

    public void readFileWithRetries(List<String> files) {
        Observable<String> filesObs = Observable.fromIterable(files);
        FileObservableSource fileObservableSource = new FileObservableSource(files.get(0));
        filesObs
                .flatMap(file -> new FileObservableSource(file))
                .retryWhen(attempts -> {
                    return attempts.take(10).delay(2, TimeUnit.SECONDS);
                })
                .subscribe(new Observer<String>() {

                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(String s) {
                        System.out.println(s);
                    }

                    @Override
                    public void onError(Throwable e) {
                        System.out.println("Error!");
                    }

                    @Override
                    public void onComplete() {
                        System.out.println("completed!");
                    }
                });
    }

    static class FileObservableSource implements ObservableSource<String> {
        private final String filename;

        FileObservableSource(String filename) {
            this.filename = filename;
        }

        @Override
        public void subscribe(Observer<? super String> observer) {
            System.out.println(String.format("Trying to read file %s", Thread.currentThread().getName()));
            try {
                Files.lines(Paths.get(filename)).forEach(observer::onNext);
                observer.onComplete();
            } catch (Exception e) {
                observer.onError(e);
            }
        }
    }
}