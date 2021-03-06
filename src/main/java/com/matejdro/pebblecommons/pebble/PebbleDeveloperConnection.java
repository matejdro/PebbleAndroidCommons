package com.matejdro.pebblecommons.pebble;

import android.content.Context;

import com.getpebble.android.kit.Constants;
import com.getpebble.android.kit.PebbleKit;
import com.matejdro.pebblecommons.util.TextUtil;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class PebbleDeveloperConnection extends WebSocketClient
{
	private List<DeveloperConnectionResult> waitingTasks = Collections.synchronizedList(new LinkedList<DeveloperConnectionResult>());

	private Context context;

	public PebbleDeveloperConnection(Context context) throws URISyntaxException
	{
		super(new URI("ws://127.0.0.1:9000"), new Draft_17(), null, 200);

		this.context = context;
	}

	@Override
	public void onOpen(ServerHandshake handshakedata)
	{
	}

	@Override
	public void onMessage(String message)
	{
	}

	@Override
	public void onMessage(ByteBuffer bytes)
	{
		int source = bytes.get();
		if (source == 0) //Message from watch
		{
			//noinspection unused
			short size = bytes.getShort();
			short endpoint = bytes.getShort();
			if (endpoint == 6000) //APP_INSTALL_MANAGER
			{
				int cmd = bytes.get();
				if (cmd == 7) //UUID of the active app
				{
					UUID receivedUUID = new UUID(bytes.getLong(), bytes.getLong());
					completeWaitingTasks(DeveloperConnectionTaskType.GET_CURRENT_RUNNING_APP_SDK_2, receivedUUID);
				}
				else if (cmd == 1) //List of all installed apps
				{
					List<PebbleApp> installedApps = PebbleApp.getFromByteBuffer(bytes);
					completeWaitingTasks(DeveloperConnectionTaskType.GET_ALL_INSTALLED_APP_META, installedApps);
				}
				else if (cmd == 5) //List of UUIDs of all installed apps
				{
					List<UUID> installedUUIDs = PebbleApp.getUUIDListFromByteBuffer(bytes);
					completeWaitingTasks(DeveloperConnectionTaskType.GET_ALL_INSTALLED_APP_UUID, installedUUIDs);
				}
			}
			else if (endpoint == 0x34) //Apps endpoint for SDK 3
			{
				int cmd = bytes.get();
				if (cmd == 1) //Current running app
				{
					UUID receivedUUID = new UUID(bytes.getLong(), bytes.getLong());
					completeWaitingTasks(DeveloperConnectionTaskType.GET_CURRENT_RUNNING_APP_SDK_3, receivedUUID);
				}
			}
		}
	}

	@Override
	public void onClose(int code, String reason, boolean remote)
	{
	}

	@Override
	public void onError(Exception ex)
	{
		ex.printStackTrace();
	}

	public UUID getCurrentRunningAppSdk2()
	{
		if (!isOpen())
			return null;

		DeveloperConnectionResult<UUID> result = new DeveloperConnectionResult(DeveloperConnectionTaskType.GET_CURRENT_RUNNING_APP_SDK_2);
		waitingTasks.add(result);

		//0x01 = CMD (PHONE_TO_WATCH)
		//0x00 0x01 = Data length (short) - 1
		//0x17 0x70 = Endpoint (6000 - APP_MANAGER)
		//0x07 = Data (7)
		byte[] requestCurrentApp = new byte[]{0x1, 0x0, 0x1, 0x17, 0x70, 0x7};
		send(requestCurrentApp);

		return result.get(5, TimeUnit.SECONDS);
	}

	public UUID getCurrentRunningAppSdk3()
	{
		if (!isOpen())
			return null;

		DeveloperConnectionResult<UUID> result = new DeveloperConnectionResult(DeveloperConnectionTaskType.GET_CURRENT_RUNNING_APP_SDK_3);
		waitingTasks.add(result);

		//0x01 = CMD (PHONE_TO_WATCH)
		//0x00 0x01 = Data length (short) - 1
		//0x00 0x34 = Endpoint
		//0x07 = Data (3) - AppRunStateRequest
		byte[] requestCurrentApp = new byte[]{0x1, 0x0, 0x1, 0x00, 0x34, 0x3};
		send(requestCurrentApp);

		return result.get(5, TimeUnit.SECONDS);
	}

	public UUID getCurrentRunningApp()
	{
		PebbleKit.FirmwareVersionInfo firmwareVersionInfo = PebbleUtil.getPebbleFirmwareVersion(context);
		if (firmwareVersionInfo == null)
			return null;

		if (firmwareVersionInfo.getMajor() >= 3)
			return getCurrentRunningAppSdk3();
		else
			return getCurrentRunningAppSdk2();
	}

	public List<PebbleApp> getInstalledPebbleApps()
	{
		if (!isOpen())
			return null;

		DeveloperConnectionResult<List<PebbleApp>> resultAppMeta = new DeveloperConnectionResult(DeveloperConnectionTaskType.GET_ALL_INSTALLED_APP_META);
		DeveloperConnectionResult<List<UUID>> resultAppUUID = new DeveloperConnectionResult(DeveloperConnectionTaskType.GET_ALL_INSTALLED_APP_UUID);

		waitingTasks.add(resultAppMeta);
		waitingTasks.add(resultAppUUID);


		//0x01 = CMD (PHONE_TO_WATCH)
		//0x00 0x01 = Data length (short) - 1
		//0x17 0x70 = Endpoint (6000 - APP_MANAGER)
		//0x01 = Data (1 = get apps meta, 5 = get apps UUID)
		byte[] request = new byte[]{0x1, 0x0, 0x1, 0x17, 0x70, 0x1};
		send(request);
		request = new byte[]{0x1, 0x0, 0x1, 0x17, 0x70, 0x5};
		send(request);


		List<PebbleApp> appList = resultAppMeta.get(5, TimeUnit.SECONDS);
		if (appList == null)
			return null;

		List<UUID> uuidList = resultAppUUID.get(5, TimeUnit.SECONDS);
		if (uuidList == null)
			return null;

		for (int i = 0; i < appList.size(); i++)
		{
			appList.get(i).uuid = uuidList.get(i);
		}

		appList.add(new PebbleApp("Sports app", Constants.SPORTS_UUID));
		appList.add(new PebbleApp("Golf app", Constants.GOLF_UUID));

		return appList;
	}

	public void sendActionACKNACKCheckmark(int notificationId, int actionId, String text)
	{
		if (!isOpen())
			return;

		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		DataOutputStream dataStream = new DataOutputStream(stream);

		try
		{
			dataStream.writeByte(1); //Message goes from phone to watch
			dataStream.writeShort(0); //Size of the messages (placeholder)
			dataStream.writeShort(3010); //Endpoint - EXTENSIBLE_NOTIFICATION
			dataStream.writeByte(17); //ACKNACK type
			writeUnsignedIntLittleEndian(dataStream, notificationId); //notificaiton id
			dataStream.writeByte(actionId); //Action ID
			dataStream.writeByte(00); //Icon attribute
			dataStream.writeByte(01); //Checkmark icon
			dataStream.writeByte(02); //Subtitle attribute
			writeUTFPebbleString(dataStream, text, 32);
		} catch (IOException e)
		{
			e.printStackTrace();
		}

		//Insert size
		int size = stream.size() - 5; //First 5 bytes do not count
		byte[] message = stream.toByteArray();
		message[1] = (byte) (size >> 8);
		message[2] = (byte) size;

		send(message);
	}


	public void sendBasicNotification(String title, String message)
	{
		if (!isOpen())
			return;

		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		DataOutputStream dataStream = new DataOutputStream(stream);

		Calendar localCalendar = Calendar.getInstance();
		String date = new SimpleDateFormat().format(localCalendar.getTime());

		int sizeTitle = Math.min(255, title.getBytes().length) + 1;
		int sizeMessage = Math.min(255, message.getBytes().length) + 1;
		int sizeDate = Math.min(255, date.getBytes().length) + 1;

		try
		{
			dataStream.writeByte(1); //Message goes from phone to watch
			dataStream.writeShort(1 + sizeTitle + sizeMessage + sizeDate); //Size of the messages(3 strings and one byte)
			dataStream.writeShort(3000); //Endpoint - NOTIFICATIONS
			dataStream.writeByte(1); //SMS Notification command


			writeLegacyPebbleString(dataStream, title);
			writeLegacyPebbleString(dataStream, message);
			writeLegacyPebbleString(dataStream, date);

		} catch (IOException e)
		{
			e.printStackTrace();
		}


		send(stream.toByteArray());
	}

	public void sendSDK3ActionACK(int notificationId)
	{
		if (!isOpen())
			return;

		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		DataOutputStream dataStream = new DataOutputStream(stream);

		try
		{
			dataStream.writeByte(1); //Message goes from phone to watch
			dataStream.writeShort(0); //Size of the messages (placeholder)
			dataStream.writeShort(0x2CB0); //Endpoint - Timeline Actions
			dataStream.writeByte(0x11); //Action response command

			//Notification key = UUID
			writeUnsignedLongLittleEndian(dataStream, notificationId); //First long
			writeUnsignedLongLittleEndian(dataStream, notificationId); //Second long

			dataStream.writeByte(0x00); //ACK
			dataStream.writeByte(0x00); //0 additional attributes
		} catch (IOException e)
		{
			e.printStackTrace();
		}

		int globalSize = stream.size() - 5; //First 5 bytes do not count
		byte[] message = stream.toByteArray();
		message[1] = (byte) (globalSize >> 8);
		message[2] = (byte) globalSize;

		send(message);
	}

	private void completeWaitingTasks(DeveloperConnectionTaskType type, Object result)
	{
		Iterator<DeveloperConnectionResult> iterator = waitingTasks.iterator();
		while (iterator.hasNext())
		{
			DeveloperConnectionResult task = iterator.next();
			if (task.getType() != type)
				continue;

			task.finished(result);
			iterator.remove();
		}
	}

	protected class DeveloperConnectionResult<T>
	{
		private T result;
		private CountDownLatch waitingLatch;
		private boolean isDone;
		private DeveloperConnectionTaskType type;

		private DeveloperConnectionResult(DeveloperConnectionTaskType type)
		{
			waitingLatch = new CountDownLatch(1);
			isDone = false;
			this.type = type;
		}

		public DeveloperConnectionTaskType getType()
		{
			return type;
		}

		protected void finished(T result)
		{
			isDone = true;
			this.result = result;
			waitingLatch.countDown();
		}

		public void cancel()
		{
			finished(null);
		}

		public boolean isCancelled()
		{
			return isDone && result == null;
		}

		public boolean isDone()
		{
			return isDone;
		}

		public T get()
		{
			try
			{
				waitingLatch.await();
			} catch (InterruptedException e)
			{
				e.printStackTrace();
			}

			return result;
		}

		public T get(long l, TimeUnit timeUnit)
		{
			try
			{
				waitingLatch.await(l, timeUnit);
			} catch (InterruptedException e)
			{
				e.printStackTrace();
			}

			return result;
		}
	}

	protected static enum DeveloperConnectionTaskType
	{
		GET_CURRENT_RUNNING_APP_SDK_2,
		GET_CURRENT_RUNNING_APP_SDK_3,
		GET_ALL_INSTALLED_APP_META,
		GET_ALL_INSTALLED_APP_UUID,
	}

	public static void writeUnsignedIntLittleEndian(DataOutputStream stream, int number) throws IOException
	{
		number = number & 0xFFFFFFFF;

		stream.write((byte) number);
		stream.write((byte) (number >> 8));
		stream.write((byte) (number >> 16));
		stream.write((byte) (number >> 24));
	}

	public static void writeUnsignedLongLittleEndian(DataOutputStream stream, long number) throws IOException
	{
		number = number & 0xFFFFFFFFFFFFFFFFL;

		stream.write((byte) number);
		stream.write((byte) (number >> 8));
		stream.write((byte) (number >> 16));
		stream.write((byte) (number >> 24));
		stream.write((byte) (number >> 32));
		stream.write((byte) (number >> 40));
		stream.write((byte) (number >> 48));
		stream.write((byte) (number >> 56));
	}

	public static void writeUnsignedShortLittleEndian(DataOutputStream stream, int number) throws IOException
	{
		number = number & 0xFFFF;

		stream.write((byte) number);
		stream.write((byte) (number >> 8));
	}

	public static String getPebbleStringFromByteBuffer(ByteBuffer buffer, int limit)
	{
		byte[] stringData = new byte[limit];

		try
		{
			buffer.get(stringData);
			String string = new String(stringData, "UTF-8");

			int end = string.indexOf(0);
			if (end >= 0)
				string = string.substring(0, end);

			return string;
		} catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}

		return "[ERROR]";
	}

	public static void writeLegacyPebbleString(DataOutputStream stream, String string)
	{
		string = TextUtil.trimString(string, 255, true);
		byte[] stringData = string.getBytes();

		try
		{
			stream.writeByte(stringData.length & 0xFF);
			stream.write(stringData);

		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public static void writeNullTerminatedPebbleStringList(DataOutputStream stream, List<String> list, int limit) throws IOException
	{
		for (String line : list)
		{
			if (limit == 0)
				break;

			line = TextUtil.trimString(line, limit - 1, true);
			byte[] bytes = line.getBytes();
			stream.write(bytes);
			stream.write(0);

			limit -= bytes.length + 1;
		}
	}


	public static void writeUTFPebbleString(DataOutputStream stream, String string, int limit) throws IOException
	{
		string = TextUtil.prepareString(string, limit);
		byte[] stringData = string.getBytes();

		writeUnsignedShortLittleEndian(stream, stringData.length);
		stream.write(stringData);
	}

}
