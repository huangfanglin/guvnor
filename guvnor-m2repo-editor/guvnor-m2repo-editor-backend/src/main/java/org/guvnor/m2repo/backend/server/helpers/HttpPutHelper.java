/*
 * Copyright 2014 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.guvnor.m2repo.backend.server.helpers;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.util.Arrays;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.plexus.util.IOUtil;
import org.guvnor.m2repo.backend.server.GuvnorM2Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpPutHelper {

    private static final Logger log = LoggerFactory.getLogger( HttpPutHelper.class );

    @Inject
    private GuvnorM2Repository m2RepoService;

    public void handle( final SecurityFilter securityFilter,
                        final HttpServletRequest request,
                        final HttpServletResponse response ) throws ServletException, IOException {

        final InputStream inputStream = request.getInputStream();
        OutputStream outputStream = null;
        int status = HttpServletResponse.SC_OK;

        try {

            //Get destination path
            final String pathInfo = URLDecoder.decode( request.getPathInfo(),
                                                       "UTF-8" );
            final File mavenRootDir = new File( m2RepoService.getM2RepositoryRootDir() );

            final File file = new File( mavenRootDir,
                                        pathInfo );

            //Create new file if it does not already exist and set status code to 201
            //See http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html Section 9.6 PUT
            if ( !file.exists() ) {
                file.getParentFile().mkdirs();
                file.createNewFile();
                status = HttpServletResponse.SC_CREATED;
            }

            if ( !securityFilter.filter( file.getCanonicalFile().toURI() ) ) {
                return;
            }

            outputStream = new BufferedOutputStream( new FileOutputStream( file ) );

            //Copy input
            IOUtil.copy( inputStream,
                         outputStream );

            response.setStatus( status );

        } catch ( FileNotFoundException e ) {
            log.error( e.toString(),
                       e );

        } catch ( IOException e ) {
            log.error( e.toString(),
                       e );

        } finally {
            if ( outputStream != null ) {
                try {
                    outputStream.flush();
                    outputStream.close();
                } catch ( IOException e ) {
                    //Swallow
                }
            }
            if ( inputStream != null ) {
                try {
                    inputStream.close();
                } catch ( IOException e ) {
                    //Swallow
                }
            }
        }

    }

    private static boolean matches( final String matchHeader,
                                    final String toMatch ) {
        String[] matchValues = matchHeader.split( "\\s*,\\s*" );
        Arrays.sort( matchValues );
        return Arrays.binarySearch( matchValues,
                                    toMatch ) > -1
                || Arrays.binarySearch( matchValues, "*" ) > -1;
    }

}
