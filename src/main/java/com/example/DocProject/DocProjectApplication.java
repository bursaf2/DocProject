package com.example.DocProject;


import com.example.DocProject.Service.FileStorageService;
import com.example.DocProject.message.ResponseMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.syncfusion.licensing.*;
import javax.annotation.Resource;

@SpringBootApplication
public class DocProjectApplication implements CommandLineRunner {
	@Resource
	FileStorageService storageService;


	public static void main(String[] args) {
		SpringApplication.run(DocProjectApplication.class, args);


		SyncfusionLicenseProvider.registerLicense("GTIlMmhhZX1ifWBmaGFifGNrfGFjYWdzY2tpYGZpZ2VoKjo/PjIpNiE3Nj4TOzIwNicnNiM2fTY3Jn0nIQ==");

	}
	@Override
	public void run(String... arg) throws Exception {
		storageService.deleteAll();
		storageService.init();




	}
}
