package com.coderhglee.batch.vo;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class InboundVo {
    private String name;
    private String protocol;
    private String server;
    private String port;
    private String user;
    private String password;
    private String local_file_path;
    private String remote_file_path;
    private String local_backup_path;
    private String passive;
    private String action;
}
