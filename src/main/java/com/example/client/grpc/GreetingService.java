package com.example.client.grpc;

import com.example.service.proto.Empty;
import com.example.service.proto.HelloRequest;
import com.example.service.proto.HelloResponse;
import com.example.service.proto.SimpleGrpc;
import io.grpc.stub.StreamObserver;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Iterator;

@RestController
@RequestMapping("client")
public class GreetingService {

    private static final StreamObserver<HelloResponse> OBJECT_STREAM_OBSERVER = new StreamObserver<>() {
        @Override
        public void onNext(HelloResponse value) {
            System.out.println(value);
        }

        @Override
        public void onError(Throwable t) {
            System.out.println(t.getMessage());
        }

        @Override
        public void onCompleted() {
            System.out.println("Computed");
        }
    };
    @GrpcClient("greetingsService")
    private SimpleGrpc.SimpleBlockingStub blockingStub;
    @GrpcClient("greetingsService")
    private SimpleGrpc.SimpleStub simpleStub;

    @GetMapping("greet")
    public String sayHello(@RequestParam String name) {
        return blockingStub.sayHello(HelloRequest.newBuilder().setName(name).build()).getMessage();
    }

    @GetMapping("data/stream")
    public void generateData(HttpServletResponse response) throws IOException {
        try (ServletOutputStream servletOutputStream = response.getOutputStream()) {
            Iterator<HelloResponse> helloResponseIterator = blockingStub.generateData(Empty.newBuilder().build());
            while (helloResponseIterator.hasNext())
                servletOutputStream.println(helloResponseIterator.next().getMessage());
        }
    }

    @GetMapping("async/greet")
    public void sayHelloAsync(@RequestParam String name) throws IOException {
        simpleStub.sayHello(HelloRequest.newBuilder().setName(name).build(), OBJECT_STREAM_OBSERVER);
    }

    @GetMapping("async/data/stream")
    public void generateDataAsync() throws IOException {
        simpleStub.generateData(Empty.newBuilder().build(), OBJECT_STREAM_OBSERVER);
    }

}