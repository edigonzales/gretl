package ch.so.agi.gretl.testutil;

import java.net.URI;

import org.junit.Assert;
import org.junit.Test;

public class SftpUriTest {
    @Test
    public void parserTest() throws Exception {
        URI uri = new URI( "sftp://ftp.umleditor.org/publisher" );
        String path=uri.getRawPath();
        Assert.assertEquals("/publisher",path);
        URI base= new URI(uri.getScheme()+"://"+uri.getHost());
        Assert.assertEquals("sftp://ftp.umleditor.org", base.toString());
    }

}
