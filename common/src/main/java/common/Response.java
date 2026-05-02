package common;

import java.io.Serializable;
import java.util.LinkedList;

import common.model.movie.Movie;

public class Response implements Serializable{
        private String status;
        private String message;
        private LinkedList<Movie> data;
        private boolean isUpdate;

        public Response(String status, String message, LinkedList<Movie>  data, boolean isUpdate){
            this.status = status;
            this.message = message;
            this.data = data;
            this.isUpdate = isUpdate;
        }

        public Response(String status, String message, boolean isUpdate){
            this.status = status;
            this.message = message;
            this.isUpdate = isUpdate;
        }

        public Response(){}

        public String getStatus(){ return status; }

        public LinkedList<Movie>  getData(){ return data; }

        public String getMessage(){ return message; }

        public boolean getUpdate(){ return isUpdate; }

        public void setStatus( String status ){ this.status = status; }

        public void setMessage( String message ){ this.message = message; }

        public void setData( LinkedList<Movie>  data ){ this.data = data; }

        public void setUpdate( boolean isUpdate ){ this.isUpdate = isUpdate; }
}
