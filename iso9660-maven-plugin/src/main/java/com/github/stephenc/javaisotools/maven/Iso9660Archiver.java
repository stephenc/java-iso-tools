package com.github.stephenc.javaisotools.maven;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import org.codehaus.plexus.archiver.AbstractArchiver;
import org.codehaus.plexus.archiver.ArchiveEntry;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.ResourceIterator;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.StringUtils;

import com.github.stephenc.javaisotools.iso9660.ConfigException;
import com.github.stephenc.javaisotools.iso9660.ISO9660Directory;
import com.github.stephenc.javaisotools.iso9660.ISO9660File;
import com.github.stephenc.javaisotools.iso9660.ISO9660RootDirectory;
import com.github.stephenc.javaisotools.iso9660.StandardConfig;
import com.github.stephenc.javaisotools.iso9660.impl.CreateISO;
import com.github.stephenc.javaisotools.iso9660.impl.ISO9660Config;
import com.github.stephenc.javaisotools.iso9660.impl.ISOImageFileHandler;
import com.github.stephenc.javaisotools.joliet.impl.JolietConfig;
import com.github.stephenc.javaisotools.maven.Iso9660ArchiverException.Type;
import com.github.stephenc.javaisotools.rockridge.impl.RockRidgeConfig;
import com.github.stephenc.javaisotools.sabre.HandlerException;
import com.github.stephenc.javaisotools.sabre.StreamHandler;

@Component(role = Archiver.class, hint = "iso9660")
public class Iso9660Archiver extends AbstractArchiver {
	private String systemId;
	private String volumeId;
	private String volumeSetId;
	private String publisher;
	private String preparer;
	private String application;

	public String getSystemId() {
		return systemId;
	}

	public void setSystemId(String systemId) {
		this.systemId = systemId;
	}

	public String getVolumeId() {
		return volumeId;
	}

	public void setVolumeId(String volumeId) {
		this.volumeId = volumeId;
	}

	public String getVolumeSetId() {
		return volumeSetId;
	}

	public void setVolumeSetId(String volumeSetId) {
		this.volumeSetId = volumeSetId;
	}

	public String getPublisher() {
		return publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	public String getPreparer() {
		return preparer;
	}

	public void setPreparer(String preparer) {
		this.preparer = preparer;
	}

	public String getApplication() {
		return application;
	}

	public void setApplication(String application) {
		this.application = application;
	}

	@Override
	protected String getArchiveType() {
		return "iso9660";
	}

	@Override
	protected void close() throws IOException {
	}

	@Override
	protected void execute() throws ArchiverException, IOException {
		File dest = getDestFile();

		if (dest == null) {
			throw new Iso9660ArchiverException(
					Iso9660ArchiverException.Type.MissingDestination,
					"You must set the destination " + getArchiveType()
							+ " file.");
		}

		if (dest.exists() && !dest.isFile()) {
			throw new Iso9660ArchiverException(
					Iso9660ArchiverException.Type.DestinationNotAFile, dest
							+ " isn't a file.");
		}

		if (dest.exists() && !dest.canWrite()) {
			throw new Iso9660ArchiverException(
					Iso9660ArchiverException.Type.DestinationReadOnly, dest
							+ " is read-only.");
		}

		ISO9660RootDirectory.MOVED_DIRECTORIES_STORE_NAME = "rr_moved";
		ISO9660RootDirectory root = new ISO9660RootDirectory();

		try {
			Map<String, ISO9660Directory> directories = new TreeMap<String, ISO9660Directory>();
			Map<String, ISO9660File> files = new TreeMap<String, ISO9660File>();

			/* Prepare a map of all paths to ISO objects and create all intermediate objects */
			directories.put("", root);

			for (ResourceIterator i = getResources(); i.hasNext();) {
				ArchiveEntry entry = i.next();

				switch (entry.getType()) {
				case ArchiveEntry.DIRECTORY: {
					ISO9660Directory dir;

					dir = new ISO9660Directory(getName(entry));

					directories.put(getPath(entry), dir);

					String path = getPath(entry);
					while ((path = getParentPath(path)) != "") {
						dir = new ISO9660Directory(getName(path));
						directories.put(path, dir);
					}

					break;
				}
				case ArchiveEntry.FILE: {
					ISO9660File file;
					ISO9660Directory dir;

					/* Create the file */
					file = new ISO9660File(
							new ArchiveEntryDataReference(entry),
							getName(entry), entry.getResource()
									.getLastModified());

					files.put(getPath(entry), file);

					String path = getPath(entry);
					while ((path = getParentPath(path)) != "") {
						dir = new ISO9660Directory(getName(path));
						directories.put(path, dir);
					}

					break;
				}
				default:
					throw new Iso9660ArchiverException(
							Type.UnsupportedEntryType, "Unknown entry type");
				}
			}

			/* Attache children to their parents */
			for (Map.Entry<String, ISO9660Directory> e : directories.entrySet()) {
				if (!e.getKey().equals("")) {
					ISO9660Directory parent = directories.get(getParentPath(e
							.getKey()));
					parent.addDirectory(e.getValue());
				}
			}

			for (Map.Entry<String, ISO9660File> e : files.entrySet()) {
				ISO9660Directory parent = directories.get(getParentPath(e
						.getKey()));
				parent.addFile(e.getValue());
			}

			StreamHandler streamHandler = new ISOImageFileHandler(new File(
					dest.getParentFile(), dest.getName()));
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
			throw new ArchiverException(e.getMessage(), e);
		} catch (FileNotFoundException e) {
			throw new ArchiverException(e.getMessage(), e);
		} catch (ConfigException e) {
			throw new ArchiverException(e.getMessage(), e);
		}
	}

	private String getParentPath(String path) {
		String parent;

		parent = path.contains("/") ? path.substring(0, path.lastIndexOf('/'))
				: "";

		return parent;
	}

	private String getPath(ArchiveEntry entry) {
		return entry.getName();
	}

	private String getName(String path) {
		return path.contains("/") ? path.substring(path.lastIndexOf('/') + 1)
				: path;
	}

	private String getName(ArchiveEntry entry) {
		return getName(entry.getName());
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
