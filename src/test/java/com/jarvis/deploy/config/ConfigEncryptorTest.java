package com.jarvis.deploy.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConfigEncryptorTest {

    private ConfigEncryptor encryptor;

    @BeforeEach
    void setUp() {
        encryptor = new ConfigEncryptor("test-passphrase-123");
    }

    @Test
    void encryptAndDecrypt_roundTrip_returnsOriginal() {
        String original = "super-secret-db-password";
        String ciphertext = encryptor.encrypt(original);
        String decrypted = encryptor.decrypt(ciphertext);
        assertEquals(original, decrypted);
    }

    @Test
    void encrypt_sameInput_producesDifferentCiphertexts() {
        String value = "my-api-key";
        String first = encryptor.encrypt(value);
        String second = encryptor.encrypt(value);
        assertNotEquals(first, second, "Each encryption should use a unique IV");
    }

    @Test
    void decrypt_withDifferentPassphrase_throwsEncryptionException() {
        String ciphertext = encryptor.encrypt("sensitive-value");
        ConfigEncryptor other = new ConfigEncryptor("wrong-passphrase");
        assertThrows(EncryptionException.class, () -> other.decrypt(ciphertext));
    }

    @Test
    void encrypt_emptyString_encryptsAndDecryptsSuccessfully() {
        String ciphertext = encryptor.encrypt("");
        assertEquals("", encryptor.decrypt(ciphertext));
    }

    @Test
    void constructor_blankPassphrase_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new ConfigEncryptor(""));
        assertThrows(IllegalArgumentException.class, () -> new ConfigEncryptor("   "));
        assertThrows(IllegalArgumentException.class, () -> new ConfigEncryptor(null));
    }

    @Test
    void decrypt_invalidBase64_throwsEncryptionException() {
        assertThrows(EncryptionException.class, () -> encryptor.decrypt("not-valid-base64!!!"));
    }

    @Test
    void encryptAndDecrypt_specialCharacters_roundTrip() {
        String value = "p@$$w0rd!#%^&*()_+={[}]|\\:;<,>.?/~`";
        assertEquals(value, encryptor.decrypt(encryptor.encrypt(value)));
    }
}
