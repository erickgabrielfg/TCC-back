package br.ufc.quixada.ecos.infrastructure.service.storage;

import br.ufc.quixada.ecos.core.storage.StorageProperties;
import br.ufc.quixada.ecos.domain.service.AnexoStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.InputStream;

@Service
public class MinioAnexoStorageService implements AnexoStorageService {

    @Autowired
    private S3Client s3Client;

    @Autowired
    private StorageProperties storageProperties;

    @Override
    public InputStream recuperar(String nomeArquivo) {
        try {
            ResponseInputStream<GetObjectResponse> obj = s3Client.getObject(
                    GetObjectRequest.builder()
                            .bucket(storageProperties.getMinio().getBucket())
                            .key(nomeArquivo)
                            .build()
            );

            return obj;

        } catch (Exception e) {
            throw new StorageException("Não foi possível recuperar arquivo.", e);
        }
    }

    @Override
    public void armazenarCaminho(NovoAnexo novoAnexo, String caminhoRelativo) {
        try {
            String caminhoArquivo = caminhoRelativo + novoAnexo.getNomeArquivo();

            InputStream is = novoAnexo.getInputStream();

            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(storageProperties.getMinio().getBucket())
                            .key(caminhoArquivo)
                            .build(),
                    RequestBody.fromBytes(is.readAllBytes())
            );

        } catch (Exception e) {
            throw new StorageException(e.getMessage(), e);
        }
    }

    @Override
    public void removerCaminho(String nomeAnexo, String caminhoRelativo) {
        try {
            String caminhoArquivo = caminhoRelativo + nomeAnexo;

            s3Client.deleteObject(
                    DeleteObjectRequest.builder()
                            .bucket(storageProperties.getMinio().getBucket())
                            .key(caminhoArquivo)
                            .build()
            );

        } catch (Exception e) {
            throw new StorageException("Falha ao apagar arquivo " + nomeAnexo, e);
        }
    }
}