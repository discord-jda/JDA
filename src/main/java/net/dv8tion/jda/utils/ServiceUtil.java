/*
 *     Copyright 2015-2016 Austin Keener & Michael Ritter
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

import javax.sound.midi.spi.MidiFileReader;
import javax.sound.midi.spi.MidiFileWriter;
import javax.sound.sampled.spi.AudioFileReader;
import javax.sound.sampled.spi.AudioFileWriter;
import javax.sound.sampled.spi.FormatConversionProvider;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

/**
 * Internal class used to load the Java Service Provider Interfaces used by JDA.
 */
public class ServiceUtil
{
    public static final Map<Class, List<String>> SERVICES;  //Populated at the bottom of this file.
    public static final String SERVICES_DIRECTORY = "META-INF/services/";

    @SuppressWarnings("unchecked")
    public static void loadServices()
    {
        File servicesJar;
        FileOutputStream fos = null;
        JarOutputStream zos = null;

        try
        {
            //Creates a new temp file to act as a dummy jar containing only our META-INF/services/ folder.
            servicesJar = Files.createTempFile("jda-services", ".jar").toFile();
            servicesJar.deleteOnExit();     //Sets to delete when the JVM closes, but it wont because it is loaded into the JVM. :/
            fos = new FileOutputStream(servicesJar);    //Opens an output stream so we can write to the file
            zos = new JarOutputStream(fos);             //Wraps the stream so we can treat it like writing to a Jar file

            //Loops through all of our services, creating files in our Jar's META-INF/services/ folder
            // using the service class's canonical name (package.package.package.classname)
            //Then writes each provider's canonical name to the created file, 1 per line.
            for (Map.Entry<Class, List<String>> service : SERVICES.entrySet())
            {
                String fileName = SERVICES_DIRECTORY + service.getKey().getCanonicalName();

                //Creates a new file in the META-INFO/services/ folder in our dummy jar.
                ZipEntry zipEntry = new ZipEntry(fileName);

                //Tells the output stream that the provided entry is the one which we are writing to.
                zos.putNextEntry(zipEntry);

                //Writes each provider's canonical name to this file, 1 per line.
                for (String provider : service.getValue())
                {
                    zos.write((provider + "\n").getBytes());
                }

                //We are done writing this service's configuration file. Close it up.
                zos.closeEntry();
            }

            //We are done with the Jar file. Close it up!
            zos.close();
            fos.close();

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


            //Give the JVM our service jars to load.
            addExtUrl.invoke(extentionLoader, servicesJar.toURI().toURL());

            //Go through each Service SPI and force it to reload them.
            //This makes the JVM rescan it's known Extensions, finding ours and loading them.
            for (Class service : SERVICES.keySet())
            {
                ServiceLoader loader = ServiceLoader.load(service);
                loader.reload();
                //System.out.println(service.getCanonicalName());
                //loader.forEach(provider -> System.out.println("  - " + provider.getClass().getName()));
            }
        }
        catch (IOException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if (zos != null) zos.close();
                if (fos != null) fos.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    static
    {
        HashMap<Class, List<String>> services = new HashMap<>();

        services.put(FormatConversionProvider.class,
            Collections.unmodifiableList(
                Arrays.asList(
                    "javazoom.spi.mpeg.sampled.convert.MpegFormatConversionProvider",   //MP3-SPI
                    "org.tritonus.sampled.convert.LawEncoder",                          //Tritonus_Remaining
                    "org.tritonus.sampled.convert.LawDecoder",                          //Tritonus_Remaining
                    "org.tritonus.sampled.convert.PCM2PCMConversionProvider",           //Tritonus_Remaining
                    "org.tritonus.sampled.convert.SampleRateConversionProvider",        //Tritonus_Remaining
                    "org.tritonus.sampled.convert.ImaAdpcmFormatConversionProvider",     //Tritonus_Remaining
                    "org.kc7bfi.jflac.sound.spi.FlacFormatConversionProvider"            //jFLAC
                )
            )
        );
        services.put(AudioFileReader.class,
            Collections.unmodifiableList(
                Arrays.asList(
                    "javazoom.spi.mpeg.sampled.file.MpegAudioFileReader",               //MP3-SPI
                    "org.tritonus.sampled.file.AuAudioFileReader",                      //Tritonus_Remaining
                    "org.tritonus.sampled.file.WaveAudioFileReader",                    //Tritonus_Remaining
                    "org.tritonus.sampled.file.AiffAudioFileReader",                    //Tritonus_Remaining
                    "net.sourceforge.jaad.spi.javasound.AACAudioFileReader",            //JAAD
                    "org.kc7bfi.jflac.sound.spi.FlacAudioFileReader"                    //jFLAC
                )
            )
        );
        services.put(AudioFileWriter.class,
            Collections.unmodifiableList(
                Arrays.asList(
                    "org.tritonus.sampled.file.AuAudioFileWriter",                      //Tritonus_Remaining
                    "org.tritonus.sampled.file.WaveAudioFileWriter",                    //Tritonus_Remaining
                    "org.tritonus.sampled.file.AiffAudioFileWriter"                     //Tritonus_Remaining
                )
            )
        );
        services.put(MidiFileWriter.class,
            Collections.unmodifiableList(
                    Collections.singletonList(
                            "org.tritonus.midi.file.StandardMidiFileWriter"                     //Tritonus_Remaining
                    )
            )
        );
        services.put(MidiFileReader.class,
            Collections.unmodifiableList(
                    Collections.singletonList(
                            "org.tritonus.midi.file.StandardMidiFileReader"                     //Tritonus_Remaining
                    )
            )
        );

        SERVICES = Collections.unmodifiableMap(services);
    }
}
