
package org.pyj.http;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;

import java.nio.charset.Charset;
import java.util.Objects;
/**
 * @Description: http请求信息类
 * @Author: pengyongjian
 * @Date: 2020-04-05 10:14
 */
public class NettyHttpRequest implements FullHttpRequest {

    private FullHttpRequest realRequest;

    private String url;

    public NettyHttpRequest(FullHttpRequest request){
        this.realRequest = request;
    }

    public String contentText(){
        return content().toString(Charset.forName("UTF-8"));
    }

    public long getLongPathValue(int index){
        String[] paths = uri().split("/");
        return Long.parseLong(paths[index]);
    }

    public String getStringPathValue(int index){
        String[] paths = uri().split("/");
        return paths[index];
    }

    public int getIntPathValue(int index){
        String[] paths = uri().split("/");
        return Integer.parseInt(paths[index]);
    }

    public boolean isAllowed(String method){
        return getMethod().name().equalsIgnoreCase(method);
    }

    public boolean matched(String path,boolean equal){
        String url = getUrl().toLowerCase();
        return equal ? Objects.equals(path,url) : url.startsWith(path);
    }

    @Override
    public ByteBuf content() {
        return realRequest.content();
    }

    @Override
    public HttpHeaders trailingHeaders() {
        return realRequest.trailingHeaders();
    }

    @Override
    public FullHttpRequest copy() {
        return realRequest.copy();
    }

    @Override
    public FullHttpRequest duplicate() {
        return realRequest.duplicate();
    }

    @Override
    public FullHttpRequest retainedDuplicate() {
        return realRequest.retainedDuplicate();
    }

    @Override
    public FullHttpRequest replace(ByteBuf byteBuf) {
        return realRequest.replace(byteBuf);
    }

    @Override
    public FullHttpRequest retain(int i) {
        return realRequest.retain(i);
    }

    @Override
    public int refCnt() {
        return realRequest.refCnt();
    }

    @Override
    public FullHttpRequest retain() {
        return realRequest.retain();
    }

    @Override
    public FullHttpRequest touch() {
        return realRequest.touch();
    }

    @Override
    public FullHttpRequest touch(Object o) {
        return realRequest.touch(o);
    }

    @Override
    public boolean release() {
        return realRequest.release();
    }

    @Override
    public boolean release(int i) {
        return realRequest.release(i);
    }

    @Override
    public HttpVersion getProtocolVersion() {
        return realRequest.protocolVersion();
    }

    @Override
    public HttpVersion protocolVersion() {
        return realRequest.protocolVersion();
    }

    @Override
    public FullHttpRequest setProtocolVersion(HttpVersion httpVersion) {
        return realRequest.setProtocolVersion(httpVersion);
    }

    @Override
    public HttpHeaders headers() {
        return realRequest.headers();
    }

    @Override
    public HttpMethod getMethod() {
        return realRequest.getMethod();
    }

    @Override
    public HttpMethod method() {
        return realRequest.method();
    }

    @Override
    public FullHttpRequest setMethod(HttpMethod httpMethod) {
        return realRequest.setMethod(httpMethod);
    }

    @Override
    public String getUri() {
        return realRequest.getUri();
    }

    @Override
    public String uri() {
        return realRequest.uri();
    }

    public String getUrl() {
        if(url != null){
            return url;
        }
        synchronized(this){
            String uri = realRequest.uri();
            if(uri.contains("?")){
                url = uri.substring(0, uri.indexOf("?"));
            } else {
                url = uri;
            }
        }
        if(url != null){
            return url;
        }
        return getUrl();
    }

    @Override
    public FullHttpRequest setUri(String s) {
        return realRequest.setUri(s);
    }

    @Override
    public DecoderResult getDecoderResult() {
        return realRequest.getDecoderResult();
    }

    @Override
    public DecoderResult decoderResult() {
        return realRequest.decoderResult();
    }

    @Override
    public void setDecoderResult(DecoderResult decoderResult) {
        realRequest.setDecoderResult(decoderResult);
    }
}
