package common;

import java.io.Serializable;

public class Response implements Serializable{
        private String status;
        private String message;
        private String data;

        public Response(String status, String message, String data){
            this.status = status;
            this.message = message;
            this.data = data;
        }

        public Response(String status, String message){
            this.status = status;
            this.message = message;
        }

        public Response(){}

        public String getStatus(){ return status; }

        public String getData(){ return data; }

        public String getMessage(){ return message; }

        public void setStatus( String status ){ this.status = status; }

        public void setMessage( String message ){ this.message = message; }

        public void setData( String data ){ this.data = data; }
}
