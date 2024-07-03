package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.io.IOException;
import java.util.Date;

/**
* Класс для работы с API Честного знака.
* Тестовое задание для компании Selsup.
 */

public class CrptApi {
    private final TimeUnit timeUnit; //представляет единицу времени для таймера.
    private final int requestLimit; //максимальное количество запросов.
    private final AtomicInteger atomicInteger = new AtomicInteger(0); //атомарный счетчик запросов.
    private Date dateOfCounterReset = new Date(); //дата последнего сброса счетчика.

    /**
     * Конструктор принимает два параметра.
     *    timeUnit - указывает промежуток времени
     *    requestLimit - максимальное количество запросов в этом промежутке времени.
     * Инициализирует переменные экземпляра значениями из параметров.
     */
    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.timeUnit = timeUnit;
        this.requestLimit = requestLimit;
    }

    /**
     * Метод реализует механизм ограничения частоты запросов API.
     * Метод синхронизирован по текущему объекту (this).
     * Это означает, что только один поток может выполнять этот метод одновременно.
     */
    public void createIntroduceGoodsDocument(Object document, String signature)
            throws IOException, InterruptedException, ParseException {

        //Эта строка начинает блок синхронизации.
        synchronized (this) {
            long currentTime = System.currentTimeMillis(); //получаем текущее время в миллисекундах
            long timePassed = currentTime - dateOfCounterReset.getTime(); //вычисляем разницу между текущим временем и временем последнего сброса счетчика.

            //Проверяем, прошло ли более одной секунды с момента последнего сброса счетчика.
            if (timePassed >= timeUnit.toMillis(1)) {
                atomicInteger.set(0); //Если прошло более одной секунды, мы устанавливаем значение счетчика в 0.
                dateOfCounterReset = new Date(currentTime); //Обновляем время последнего сброса счетчика, устанавливая его равным текущему времени.
            }

            //Этот цикл выполняется, пока значение счетчика больше или равно максимальное количество запросов, которое можно обработать за определенный период времени.
            while (atomicInteger.get() >= requestLimit) {
                wait(timeUnit.toMillis(1) - timePassed); //приостановить выполнение текущего потока.
                currentTime = System.currentTimeMillis(); //Мы обновляем значение currentTime до текущего времени.
                timePassed = currentTime - dateOfCounterReset.getTime();

                //Проверяем, прошло ли более одной секунды с момента последнего сброса счетчика.
                if (timePassed >= timeUnit.toMillis(1)) {
                    atomicInteger.set(0);
                    dateOfCounterReset = new Date(currentTime);
                }
            }

            /**
             * Этот код выполняет HTTP POST-запрос к указанному API,
             * отправляя информацию о документе в формате JSON
             */
            HttpClient httpClient = HttpClients.createDefault(); //Инициализация HTTP-клиента
            HttpPost httpPost = new HttpPost("https://ismp.crpt.ru/api/v3/lk/documents/create"); //Настройка HTTP POST-запроса

            //Сериализация объекта в JSON:
            ObjectMapper objectMapper = new ObjectMapper(); //преобразования объекта document в строку JSON
            String json = objectMapper.writeValueAsString(document); //преобразует Java-объект в его JSON-представление

            StringEntity entity = new StringEntity(json);
            httpPost.setEntity(entity); //Устанавливаем тело запроса для HttpPost
            httpPost.setHeader("Content-Type", "application/json"); //Устанавливаем заголовок Content-Type
            httpPost.setHeader("Signature", signature); //Устанавливаем заголовок Signature

            CloseableHttpResponse response = (CloseableHttpResponse) httpClient.execute(httpPost); //Отправляем HTTP-запрос и получаем ответ
            HttpEntity responseEntity = response.getEntity(); //Получаем тело ответа
            String responseString = EntityUtils.toString(responseEntity, "UTF-8"); //Преобразуем тело ответа в строку

            atomicInteger.incrementAndGet(); //Увеличиваем значение счетчика
            System.out.println("Document creation response: " + responseString); //Выводим строку с информацией о создании документа и ответе от сервера
        }
    }

    public static class Description {
        private String participantInn;

        public String getParticipantInn() {
            return participantInn;
        }

        public void setParticipantInn(String participantInn) {
            this.participantInn = participantInn;
        }
    }

    public static class Product {
        private String certificate_document;
        private String certificate_document_date;
        private String certificate_document_number;
        private String owner_inn;
        private String producer_inn;
        private String production_date;
        private String tnved_code;
        private String uit_code;
        private String uitu_code;

        public String getCertificate_document() {
            return certificate_document;
        }

        public void setCertificate_document(String certificate_document) {
            this.certificate_document = certificate_document;
        }

        public String getCertificate_document_date() {
            return certificate_document_date;
        }

        public void setCertificate_document_date(String certificate_document_date) {
            this.certificate_document_date = certificate_document_date;
        }

        public String getCertificate_document_number() {
            return certificate_document_number;
        }

        public void setCertificate_document_number(String certificate_document_number) {
            this.certificate_document_number = certificate_document_number;
        }

        public String getOwner_inn() {
            return owner_inn;
        }

        public void setOwner_inn(String owner_inn) {
            this.owner_inn = owner_inn;
        }

        public String getProducer_inn() {
            return producer_inn;
        }

        public void setProducer_inn(String producer_inn) {
            this.producer_inn = producer_inn;
        }

        public String getProduction_date() {
            return production_date;
        }

        public void setProduction_date(String production_date) {
            this.production_date = production_date;
        }

        public String getTnved_code() {
            return tnved_code;
        }

        public void setTnved_code(String tnved_code) {
            this.tnved_code = tnved_code;
        }

        public String getUit_code() {
            return uit_code;
        }

        public void setUit_code(String uit_code) {
            this.uit_code = uit_code;
        }

        public String getUitu_code() {
            return uitu_code;
        }

        public void setUitu_code(String uitu_code) {
            this.uitu_code = uitu_code;
        }
    }

    public static class Root {
        private Description description;
        private String doc_id;
        private String doc_status;
        private String doc_type;
        private boolean importRequest;
        private String owner_inn;
        private String participant_inn;
        private String producer_inn;
        private String production_date;
        private String production_type;
        private ArrayList<Product> products;
        private String reg_date;
        private String reg_number;

        public Description getDescription() {
            return description;
        }

        public void setDescription(Description description) {
            this.description = description;
        }

        public String getDoc_id() {
            return doc_id;
        }

        public void setDoc_id(String doc_id) {
            this.doc_id = doc_id;
        }

        public String getDoc_status() {
            return doc_status;
        }

        public void setDoc_status(String doc_status) {
            this.doc_status = doc_status;
        }

        public String getDoc_type() {
            return doc_type;
        }

        public void setDoc_type(String doc_type) {
            this.doc_type = doc_type;
        }

        public boolean isImportRequest() {
            return importRequest;
        }

        public void setImportRequest(boolean importRequest) {
            this.importRequest = importRequest;
        }

        public String getOwner_inn() {
            return owner_inn;
        }

        public void setOwner_inn(String owner_inn) {
            this.owner_inn = owner_inn;
        }

        public String getParticipant_inn() {
            return participant_inn;
        }

        public void setParticipant_inn(String participant_inn) {
            this.participant_inn = participant_inn;
        }

        public String getProducer_inn() {
            return producer_inn;
        }

        public void setProducer_inn(String producer_inn) {
            this.producer_inn = producer_inn;
        }

        public String getProduction_date() {
            return production_date;
        }

        public void setProduction_date(String production_date) {
            this.production_date = production_date;
        }

        public String getProduction_type() {
            return production_type;
        }

        public void setProduction_type(String production_type) {
            this.production_type = production_type;
        }

        public ArrayList<Product> getProducts() {
            return products;
        }

        public void setProducts(ArrayList<Product> products) {
            this.products = products;
        }

        public String getReg_date() {
            return reg_date;
        }

        public void setReg_date(String reg_date) {
            this.reg_date = reg_date;
        }

        public String getReg_number() {
            return reg_number;
        }

        public void setReg_number(String reg_number) {
            this.reg_number = reg_number;
        }
    }

    public static void main(String[] args)
            throws IOException, InterruptedException, ParseException {

        CrptApi crptApi = new CrptApi(TimeUnit.SECONDS, 5);

        Object document = new Root();
        String signature = "signature";

        crptApi.createIntroduceGoodsDocument(document, signature);
    }
}
