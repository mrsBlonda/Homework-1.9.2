import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHeaders;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;


public class Main {
    public static final String URI_NASA = "https://api.nasa.gov/planetary/apod?api_key=cUZTgwbL3zE4uaw1dbMGLrZyoQiRxzwiUrxjMOWW";
    public static ObjectMapper mapper = new ObjectMapper();
    public static void main(String[] args) throws IOException {
        CloseableHttpClient httpClient = HttpClientBuilder.create()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectTimeout(5000)    // максимальное время ожидание подключения к серверу
                        .setSocketTimeout(30000)    // максимальное время ожидания получения данных
                        .setRedirectsEnabled(false) // возможность следовать редиректу в ответе
                        .build())
                .build();

        HttpGet request = new HttpGet(URI_NASA);
        request.setHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
        CloseableHttpResponse response = httpClient.execute(request);

        //String body = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
        //System.out.println(body);

        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        List<Nasa> list = mapper.readValue(
                response.getEntity().getContent(), new TypeReference<>() {
                }
        );
        list.forEach(System.out::println);
        Nasa nasa = list.get(0);
        String url = nasa.getUrl();
        System.out.println(url);

        String[] nameFoto = url.split("/");
        String nameFile = nameFoto[(nameFoto.length-1)];
        System.out.println(nameFile);

        HttpGet requestFoto = new HttpGet(url);
        CloseableHttpResponse responseFoto = httpClient.execute(requestFoto);
        byte[] fotoBytes = responseFoto.getEntity().getContent().readAllBytes();

        try (FileOutputStream fos = new FileOutputStream(nameFile)) {
            fos.write(fotoBytes, 0, fotoBytes.length);
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }

        response.close();
        responseFoto.close();
        httpClient.close();
    }


}

