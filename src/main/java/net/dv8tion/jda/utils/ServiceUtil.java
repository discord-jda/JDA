/**
 *    Copyright 2015-2016 Austin Keener & Michael Ritter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.dv8tion.jda.utils;

import sun.net.spi.nameservice.dns.DNSNameService;

import javax.sound.midi.spi.MidiDeviceProvider;
import javax.sound.midi.spi.MidiFileReader;
import javax.sound.midi.spi.MidiFileWriter;
import javax.sound.midi.spi.SoundbankReader;
import javax.sound.sampled.spi.AudioFileReader;
import javax.sound.sampled.spi.AudioFileWriter;
import javax.sound.sampled.spi.FormatConversionProvider;
import javax.sound.sampled.spi.MixerProvider;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Internal class used to load the Java Service Provider Interfaces used by JDA.
 */
public class ServiceUtil
{
    public static void loadServices()
    {
        URI uri = null;
        List<Path> dependencyJars = new LinkedList<Path>();
        try
        {
            uri = ServiceUtil.class.getProtectionDomain().getCodeSource().getLocation().toURI();
            if (uri.toString().endsWith("jar")) //We only care about loading if we are in a Jar. In IDE we should have stuff in classpath.
            {
                //Change from the normal file URI to a jar URI so that we can read use the JDK JAR provider to read the
                // jar file's internals.
                String uriMod = "jar:" + uri.toString() + "!/"; //Specifically this sets us at the internal root of the jar
                URI newUri = new URI(uriMod);

                //Create a virtual file system based on the jar.
                FileSystem fs = FileSystems.newFileSystem(newUri, Collections.<String, Object>emptyMap());

                //This returns the root of the jar. We can assume that there exists a root and it is /
                Path rootPath = fs.getRootDirectories().iterator().next();

                //Loops through all of the files within the root directory.
                // We start at a depth of 1 because, techincally, a depth of 0 returns the TRUE root, or the
                // folder that contains the root folder. We want the internals of the "root" folder, or /
                // Furthermore, we don't want to search further than the root folder because we are only expecting
                // dependency jars in the root folder. Setting depth higher would search further into the jar.
                Files.walk(rootPath, 1).forEach(path ->
                {
                    if(path.toString().endsWith("jar"))
                    {
                        dependencyJars.add(path);
                    }
                });

                //Creates a temp directory in the OS specific Temporary files folder.
                Path tempDir = Files.createTempDirectory("JDA-services");
                tempDir.toFile().deleteOnExit();    //Delete the folder on exit. Resource cleanup.

                //The DNSNameService class is loaded by the Launcher.ExtLoader, also known as the JVM Extension Loader.
                //This is just a quick shortcut to that specific Loader. The Extension Loader is also the highest loader
                // accessible by programs. I say "accessible" because techinically there is a higher Classloader,
                // the Bootstrap loader, however it is inaccessible ([native] code), but even if we could access it
                // we wouldn't want to in this situation. We specifically need the ExtLoader to load Java Extentions.
                //I mentioned that this is just a shortcut: The other method of getting the ExtLoader is to
                // first get the loader of the current class and call .getParent() until you get the ExtLoader. You'll
                // know that you have the ExtLoader when the .getParent() call returns null. The parent of the ExtLoader
                // is the Bootstrap Loader which is defined by null.
                ClassLoader extentionLoader = DNSNameService.class.getClassLoader();

                //Get the "addExtUrl" method from the ExtLoader class. As a note, in the JVM code, this method literally
                // just calls super.addUrl(URL) which is a URLClassLoader method. Technically we could have go the
                // addURL(URL) method instead by using getClass().getSuperClass() (which gives us the URLClassLoader
                // instead of the ExtLoader) however, I chose to use the ExtLoader method because JVM implementation may
                // change in the future.
                Method addExtUrl = extentionLoader.getClass().getDeclaredMethod("addExtURL", URL.class);

                //Make the method public accessible.
                addExtUrl.setAccessible(true);

                //Create temp files for each of our service dependencies inside of the temp directory we created
                for (Path jarServicePath : dependencyJars)
                {
                    //Cuts off the .jar so that it can be properly places as the "suffix" for the temp file
                    String fileName = jarServicePath.getFileName().toString().replace(".jar", "");

                    //Creates an empty temp file, placing it our temp directory.
                    Path tempServiceFile = Files.createTempFile(tempDir, fileName, ".jar");
                    tempServiceFile.toFile().deleteOnExit();

                    //Copies the file our of the jar and into the temp file that we created.
                    // Earlier we created an empty temp file, so we need to REPLACE_EXISTING.
                    Path ps = Files.copy(jarServicePath, tempServiceFile, StandardCopyOption.REPLACE_EXISTING);

                    //Give the JVM our service jars to load.
                    addExtUrl.invoke(extentionLoader, ps.toUri().toURL());
                }

                //Services that we want to reload.
                Class[] services = new Class[]{
                        MixerProvider.class,
                        FormatConversionProvider.class,
                        AudioFileReader.class,
                        AudioFileWriter.class,
                        MidiDeviceProvider.class,
                        SoundbankReader.class,
                        MidiFileWriter.class,
                        MidiFileReader.class
                };

                //Go through each Service SPI and force it to reload them.
                //This makes the JVM rescan it's known Extensions, finding ours and loading them.
                for (Class service : services)
                {
                    ServiceLoader.loadInstalled(service); //.forEach(provider -> System.out.println("  - " + provider.getClass().getName()));
                }
            }
        }
        catch (URISyntaxException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (NoSuchMethodException e)
        {
            e.printStackTrace();
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }
        catch (InvocationTargetException e)
        {
            e.printStackTrace();
        }
    }
}
