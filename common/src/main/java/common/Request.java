package common;

import java.io.Serializable;

public class Request implements Serializable{
        private String command;
        private Object data;
        private String[] args;

        public Request(String command, String[] args, Object data){
            this.command = command;
            this.data = data;
            this.args = args;
        }
        
        public Request(){}

        public String getCommand(){ return command; }
        public Object getData(){ return data; }
        public String[] getArgs(){ return args; }

        public void setCommand(String command){ this.command = command; }
        public void setData(Object data ){ this.data = data; }
        public void setArgs(String[] args){ this.args = args;}
        
}
