package com.github.stephenc.javaisotools.maven;

import java.io.File;
import java.io.FileNotFoundException;

import com.github.stephenc.javaisotools.sabre.StreamHandler;
import de.tu_darmstadt.informatik.rbg.hatlak.iso9660.ConfigException;
import de.tu_darmstadt.informatik.rbg.hatlak.iso9660.ISO9660RootDirectory;
import de.tu_darmstadt.informatik.rbg.hatlak.iso9660.StandardConfig;
import de.tu_darmstadt.informatik.rbg.hatlak.iso9660.impl.CreateISO;
import de.tu_darmstadt.informatik.rbg.hatlak.iso9660.impl.ISO9660Config;
import de.tu_darmstadt.informatik.rbg.hatlak.iso9660.impl.ISOImageFileHandler;
import de.tu_darmstadt.informatik.rbg.hatlak.joliet.impl.JolietConfig;
import de.tu_darmstadt.informatik.rbg.hatlak.rockridge.impl.RockRidgeConfig;
import com.github.stephenc.javaisotools.sabre.HandlerException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.StringUtils;

/**
 * Creates an iso9660 image.
 *
 * @goal iso
 */
public class PackageMojo extends AbstractMojo {

    /**
     * The directory to place the iso9660 image.
     *
     * @parameter expression="${project.build.directory}"
     */
    private File outputDirectory;

    /**
     * The directory to capture the content from.
     *
     * @parameter expression="${project.build.outputDirectory}"
     */
    private File inputDirectory;

    /**
     * The name of the file to create.
     *
     * @parameter expression="${project.build.finalName}.${project.packaging}"
     */
    private String finalName;

    /**
     * The system id.
     *
     * @parameter
     */
    private String systemId;

    /**
     * The volume id.
     *
     * @parameter default-value="${project.artifactId}"
     */
    private String volumeId;

    /**
     * The volume set id.
     *
     * @parameter
     */
    private String volumeSetId;

    /**
     * The publisher.
     *
     * @parameter default-value="${project.organization.name}"
     */
    private String publisher;

    /**
     * The preparer.
     *
     * @parameter default-value="${project.organization.name}"
     */
    private String preparer;

    /**
     * The application.
     *
     * @parameter default-value="iso9660-maven-plugin"
     */
    private String application;

    /**
     * The volume sequence number.
     *
     * @parameter
     */
    private Integer volumeSequenceNumber;

    /**
     * The volume set size.
     */
    private Integer volumeSetSize;

    public void execute() throws MojoExecutionException, MojoFailureException {
        if (outputDirectory.isFile()) {
            throw new MojoExecutionException("Output directory: " + outputDirectory + " is a file");
        }
        outputDirectory.mkdirs();
        if (!outputDirectory.isDirectory()) {
            throw new MojoExecutionException("Could not create output directory: " + outputDirectory);
        }
        // Directory hierarchy, starting from the root
        ISO9660RootDirectory.MOVED_DIRECTORIES_STORE_NAME = "rr_moved";
        ISO9660RootDirectory root = new ISO9660RootDirectory();

        try {
            if (inputDirectory.isDirectory()) {
                root.addContentsRecursively(inputDirectory);
            }

            StreamHandler streamHandler = new ISOImageFileHandler(new File(outputDirectory, finalName));
            CreateISO iso = new CreateISO(streamHandler, root);
            ISO9660Config iso9660Config = new ISO9660Config();
            iso9660Config.allowASCII(false);
            iso9660Config.setInterchangeLevel(1);
            iso9660Config.restrictDirDepthTo8(true);
            iso9660Config.forceDotDelimiter(true);
            applyConfig(iso9660Config);
            RockRidgeConfig rrConfig = new RockRidgeConfig();
            rrConfig.setMkisofsCompatibility(false);
            rrConfig.hideMovedDirectoriesStore(true);
            rrConfig.forcePortableFilenameCharacterSet(true);

            JolietConfig jolietConfig = new JolietConfig();
            jolietConfig.forceDotDelimiter(true);
            applyConfig(jolietConfig);

            iso.process(iso9660Config, rrConfig, jolietConfig, null);
        } catch (HandlerException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        } catch (FileNotFoundException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        } catch (ConfigException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    private void applyConfig(StandardConfig config) throws ConfigException {
        if (StringUtils.isNotEmpty(systemId)) {
            config.setSystemID(systemId);
        }
        if (StringUtils.isNotEmpty(volumeId)) {
            config.setVolumeID(volumeId);
        }
        if (StringUtils.isNotEmpty(volumeSetId)) {
            config.setVolumeSetID(volumeSetId);
        }
        if (StringUtils.isNotEmpty(publisher)) {
            config.setPublisher(publisher);
        }
        if (StringUtils.isNotEmpty(preparer)) {
            config.setDataPreparer(preparer);
        }
        if (StringUtils.isNotEmpty(application)) {
            config.setApp(application);
        }
    }
}