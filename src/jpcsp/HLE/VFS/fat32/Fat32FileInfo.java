/*
This file is part of jpcsp.

Jpcsp is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Jpcsp is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Jpcsp.  If not, see <http://www.gnu.org/licenses/>.
 */
package jpcsp.HLE.VFS.fat32;

import static jpcsp.HLE.modules.IoFileMgrForUser.PSP_O_RDWR;

import java.util.LinkedList;
import java.util.List;

import jpcsp.HLE.VFS.IVirtualFile;
import jpcsp.HLE.VFS.IVirtualFileSystem;
import jpcsp.HLE.kernel.types.ScePspDateTime;
import jpcsp.util.Utilities;

public class Fat32FileInfo {
	private String dirName;
	private String fileName;
	private String fileName83;
	private boolean directory;
	private boolean readOnly;
	private ScePspDateTime lastModified;
	private long fileSize;
	private int[] clusters;
	private List<Fat32FileInfo> children;
	private IVirtualFile vFile;
	private boolean vFileOpen;
	private byte[] fileData;
	private Fat32FileInfo parentDirectory;

	public Fat32FileInfo(String dirName, String fileName, boolean directory, boolean readOnly, ScePspDateTime lastModified, long fileSize) {
		this.dirName = dirName;
		this.fileName = fileName;
		this.directory = directory;
		this.readOnly = readOnly;
		this.lastModified = lastModified;
		this.fileSize = fileSize;
	}

	public String getDirName() {
		return dirName;
	}

	public void setDirName(String dirName) {
		this.dirName = dirName;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFullFileName() {
		if (dirName == null) {
			return fileName;
		}
		return dirName + '/' + fileName;
	}

	public boolean isDirectory() {
		return directory;
	}

	public void setDirectory(boolean directory) {
		this.directory = directory;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	public ScePspDateTime getLastModified() {
		return lastModified;
	}

	public void setLastModified(ScePspDateTime lastModified) {
		this.lastModified = lastModified;
	}

	public long getFileSize() {
		return fileSize;
	}

	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}

	public int[] getClusters() {
		return clusters;
	}

	public void setClusters(int[] clusters) {
		this.clusters = clusters;
	}

	public void addChild(Fat32FileInfo fileInfo) {
		if (children == null) {
			children = new LinkedList<Fat32FileInfo>();
		}

		children.add(fileInfo);
		fileInfo.setParentDirectory(this);;
	}

	public List<Fat32FileInfo> getChildren() {
		return children;
	}

	public IVirtualFile getVirtualFile(IVirtualFileSystem vfs) {
		if (!vFileOpen) {
			vFile = vfs.ioOpen(getFullFileName(), PSP_O_RDWR, 0);
			vFileOpen = true;
		}

		return vFile;
	}

	public byte[] getFileData() {
		return fileData;
	}

	public void setFileData(byte[] fileData) {
		this.fileData = fileData;
	}

	public Fat32FileInfo getParentDirectory() {
		return parentDirectory;
	}

	public void setParentDirectory(Fat32FileInfo parentDirectory) {
		this.parentDirectory = parentDirectory;
	}

	public boolean isRootDirectory() {
		return isDirectory() && dirName == null && fileName == null;
	}

	public String getFileName83() {
		return fileName83;
	}

	public void setFileName83(String fileName83) {
		this.fileName83 = fileName83;
	}

	public Fat32FileInfo getChildByFileName83(String fileName83) {
		if (children != null) {
			for (Fat32FileInfo child : children) {
				String childFileName83 = child.getFileName83();
				if (childFileName83 != null && fileName83.equals(childFileName83)) {
					return child;
				}
			}
		}

		return null;
	}

	public boolean hasCluster(int cluster) {
		if (clusters != null) {
			for (int i = 0; i < clusters.length; i++) {
				if (clusters[i] == cluster) {
					return true;
				}
			}
		}

		return false;
	}

	public void addCluster(int cluster) {
		clusters = Utilities.extendArray(clusters, 1);
		clusters[clusters.length - 1] = cluster;
	}

	public int getFirstCluster() {
		if (clusters == null || clusters.length == 0) {
			return 0;
		}

		return clusters[0];
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();

		if (getFullFileName() == null) {
			s.append("[ROOT]");
		} else {
			s.append(getFullFileName());
		}

		if (fileName83 != null) {
			s.append(String.format("('%s')", fileName83));
		}

		if (directory) {
			s.append(", directory");
		}

		if (readOnly) {
			s.append(", readOnly");
		}

		s.append(String.format(", size=0x%X", fileSize));

		if (clusters != null) {
			s.append(", clusters=[");
			for (int i = 0; i < clusters.length; i++) {
				if (i > 0) {
					s.append(", ");
				}
				s.append(String.format("0x%X", clusters[i]));
			}
			s.append("]");
		}

		return s.toString();
	}
}