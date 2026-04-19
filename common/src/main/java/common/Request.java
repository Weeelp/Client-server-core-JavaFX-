package common;

import java.io.Serializable;

public class Request implements Serializable{
        private String command;
        private Object data;
        private String[] args;
        private String[] user;

        public Request(String command, String[] args, Object data, String[] user){
            this.command = command;
            this.data = data;
            this.args = args;
            this.user = user;
        }
        
        public Request(){}

        public String getCommand(){ return command; }
        public Object getData(){ return data; }
        public String[] getArgs(){ return args; }
        public String[] getUser(){ return user; }

        public void setCommand(String command){ this.command = command; }
        public void setData(Object data ){ this.data = data; }
        public void setArgs(String[] args){ this.args = args;}
        public void setUser(String[] user){ this.user = user;} 
}
