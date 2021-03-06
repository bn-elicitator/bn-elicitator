class UrlMappings {

	static mappings = {

		"/content/admin/$action?/$id?" ( controller: "contentEdit" )
		"/content/$page?"              ( controller: "contentView" )
		"/probabilities/$action?/$id?" ( controller: "das2004" )

		"500"                          ( controller:'error', exception: Exception )
		"404"                          ( controller: 'error', action: 'notFound' )
		"/admin/manage/$action?"       ( controller: "adminManage" )
		"/auth/oauth/$action?"         ( controller: "springSecurityOAuth" )

		"/$controller/$action?/$id?"   ( )
		"/"                            ( controller:'home' )


	}
}
