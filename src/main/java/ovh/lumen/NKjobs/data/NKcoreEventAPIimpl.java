package ovh.lumen.NKjobs.data;

import ovh.lumen.NKcore.api.NKcoreEventAPI;
import ovh.lumen.NKcore.api.data.packet.ErrorResponse;
import ovh.lumen.NKcore.api.data.packet.PlayersQueryResponse;
import ovh.lumen.NKcore.api.data.packet.ReceivedData;
import ovh.lumen.NKcore.api.data.packet.ServersQueryResponse;

public class NKcoreEventAPIimpl implements NKcoreEventAPI
{
	@Override
	public void onDataReceive(ReceivedData receivedData)
	{
		System.out.println(receivedData.getServerSource());
		System.out.println(receivedData.getPluginSource());
		System.out.println(receivedData.getData());
	}

	@Override
	public void onErrorResponse(ErrorResponse errorResponse)
	{
		System.out.println(errorResponse.getServerSource());
		System.out.println(errorResponse.getPluginSource());
		System.out.println(errorResponse.getErrorResponseType().toString());
		System.out.println(errorResponse.getData());
	}

	@Override
	public void onPlayersQueryResponse(PlayersQueryResponse allPlayersQueryResponse)
	{
		System.out.println(allPlayersQueryResponse.getServerSource());
		System.out.println(allPlayersQueryResponse.getPluginSource());
		System.out.println(allPlayersQueryResponse.getData());
		allPlayersQueryResponse.getPlayersInfo().forEach(playerInfo -> {
			System.out.println("#########################");
			System.out.println(playerInfo.getName());
			System.out.println(playerInfo.getUuid().toString());
			System.out.println(playerInfo.getServerName());
		});
	}

	@Override
	public void onServersQueryResponse(ServersQueryResponse allServersQueryResponse)
	{
		System.out.println(allServersQueryResponse.getServerSource());
		System.out.println(allServersQueryResponse.getPluginSource());
		System.out.println(allServersQueryResponse.getData());
		allServersQueryResponse.getServers().forEach(System.out::println);
	}
}
