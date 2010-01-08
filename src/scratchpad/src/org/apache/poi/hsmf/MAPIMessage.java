/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.apache.poi.hsmf;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.poi.hsmf.datatypes.AttachmentChunks;
import org.apache.poi.hsmf.datatypes.ChunkGroup;
import org.apache.poi.hsmf.datatypes.Chunks;
import org.apache.poi.hsmf.datatypes.NameIdChunks;
import org.apache.poi.hsmf.datatypes.RecipientChunks;
import org.apache.poi.hsmf.datatypes.StringChunk;
import org.apache.poi.hsmf.exceptions.ChunkNotFoundException;
import org.apache.poi.hsmf.parsers.POIFSChunkParser;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

/**
 * Reads an Outlook MSG File in and provides hooks into its data structure.
 *
 * @author Travis Ferguson
 */
public class MAPIMessage {
	private POIFSFileSystem fs;
	
	private Chunks mainChunks;
	private NameIdChunks nameIdChunks;
	private RecipientChunks recipientChunks;
	private AttachmentChunks[] attachmentChunks;

	/**
	 * Constructor for creating new files.
	 *
	 */
	public MAPIMessage() {
		//TODO make writing possible
	}


	/**
	 * Constructor for reading MSG Files from the file system.
	 * @param filename
	 * @throws IOException
	 */
	public MAPIMessage(String filename) throws IOException {
		this(new FileInputStream(new File(filename)));
	}

	/**
	 * Constructor for reading MSG Files from an input stream.
	 * @param in
	 * @throws IOException
	 */
	public MAPIMessage(InputStream in) throws IOException {
	   this(new POIFSFileSystem(in));
	}
   /**
    * Constructor for reading MSG Files from an input stream.
    * @param in
    * @throws IOException
    */
   public MAPIMessage(POIFSFileSystem fs) throws IOException {
		this.fs = fs;
		
		// Grab all the chunks
		ChunkGroup[] chunkGroups = POIFSChunkParser.parse(this.fs);
		
		// Grab interesting bits
		ArrayList<AttachmentChunks> attachments = new ArrayList<AttachmentChunks>();
		for(ChunkGroup group : chunkGroups) {
		   // Should only ever be one of these
		   if(group instanceof Chunks) {
		      mainChunks = (Chunks)group;
		   } else if(group instanceof NameIdChunks) {
		      nameIdChunks = (NameIdChunks)group;
		   } else if(group instanceof RecipientChunks) {
		      recipientChunks = (RecipientChunks)group;
		   }
		   
		   // Add to list(s)
		   if(group instanceof AttachmentChunks) {
		      attachments.add((AttachmentChunks)group);
		   }
		}
		attachmentChunks = attachments.toArray(new AttachmentChunks[attachments.size()]);
	}


	/**
	 * Gets a string value based on the passed chunk.
	 * @throws ChunkNotFoundException if the chunk isn't there
	 */
	public String getStringFromChunk(StringChunk chunk) throws ChunkNotFoundException {
	   if(chunk == null) {
	      throw new ChunkNotFoundException();
	   }
	   return chunk.getValue();
	}


	/**
	 * Gets the plain text body of this Outlook Message
	 * @return The string representation of the 'text' version of the body, if available.
	 * @throws ChunkNotFoundException
	 */
	public String getTextBody() throws ChunkNotFoundException {
		return getStringFromChunk(mainChunks.textBodyChunk);
	}

	/**
	 * Gets the subject line of the Outlook Message
	 * @throws ChunkNotFoundException
	 */
	public String getSubject() throws ChunkNotFoundException {
		return getStringFromChunk(mainChunks.subjectChunk);
	}

	/**
	 * Gets the display value of the "TO" line of the outlook message
	 * This is not the actual list of addresses/values that will be sent to if you click Reply in the email.
	 * @throws ChunkNotFoundException
	 */
	public String getDisplayTo() throws ChunkNotFoundException {
		return getStringFromChunk(mainChunks.displayToChunk);
	}

	/**
	 * Gets the display value of the "FROM" line of the outlook message
	 * This is not the actual address that was sent from but the formated display of the user name.
	 * @throws ChunkNotFoundException
	 */
	public String getDisplayFrom() throws ChunkNotFoundException {
		return getStringFromChunk(mainChunks.displayFromChunk);
	}

	/**
	 * Gets the display value of the "TO" line of the outlook message
	 * This is not the actual list of addresses/values that will be sent to if you click Reply in the email.
	 * @throws ChunkNotFoundException
	 */
	public String getDisplayCC() throws ChunkNotFoundException {
		return getStringFromChunk(mainChunks.displayCCChunk);
	}

	/**
	 * Gets the display value of the "TO" line of the outlook message
	 * This is not the actual list of addresses/values that will be sent to if you click Reply in the email.
	 * @throws ChunkNotFoundException
	 */
	public String getDisplayBCC() throws ChunkNotFoundException {
		return getStringFromChunk(mainChunks.displayBCCChunk);
	}


	/**
	 * Gets the conversation topic of the parsed Outlook Message.
	 * This is the part of the subject line that is after the RE: and FWD:
	 * @throws ChunkNotFoundException
	 */
	public String getConversationTopic() throws ChunkNotFoundException {
		return getStringFromChunk(mainChunks.conversationTopic);
	}

	/**
	 * Gets the message class of the parsed Outlook Message.
	 * (Yes, you can use this to determine if a message is a calendar item, note, or actual outlook Message)
	 * For emails the class will be IPM.Note
	 *
	 * @throws ChunkNotFoundException
	 */
	public String getMessageClass() throws ChunkNotFoundException {
		return getStringFromChunk(mainChunks.messageClass);
	}

	
	/**
	 * Gets the main, core details chunks
	 */
	public Chunks getMainChunks() {
	   return mainChunks;
	}
	/**
	 * Gets the recipient details chunks, or
	 *  null if there aren't any
	 */
	public RecipientChunks getRecipientDetailsChunks() {
	   return recipientChunks;
	}
	/**
	 * Gets the Name ID chunks, or
    *  null if there aren't any
	 */
	public NameIdChunks getNameIdChunks() {
	   return nameIdChunks;
	}
	/**
	 * Gets the message attachments.
	 */
	public AttachmentChunks[] getAttachmentFiles() {
		return attachmentChunks;
	}
}
