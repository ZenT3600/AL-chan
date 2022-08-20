package it.matteoleggio.alchan.data.response.overview

import com.google.gson.annotations.SerializedName


data class Media (

  @SerializedName("__typename"        ) var _typename         : String?                  = null,
  @SerializedName("id"                ) var id                : Int?                     = null,
  @SerializedName("idMal"             ) var idMal             : Int?                     = null,
  @SerializedName("title"             ) var title             : Title?                   = Title(),
  @SerializedName("type"              ) var type              : String?                  = null,
  @SerializedName("format"            ) var format            : String?                  = null,
  @SerializedName("status"            ) var status            : String?                  = null,
  @SerializedName("description"       ) var description       : String?                  = null,
  @SerializedName("startDate"         ) var startDate         : StartDate?               = StartDate(),
  @SerializedName("endDate"           ) var endDate           : EndDate?                 = EndDate(),
  @SerializedName("season"            ) var season            : String?                  = null,
  @SerializedName("seasonYear"        ) var seasonYear        : Int?                     = null,
  @SerializedName("seasonInt"         ) var seasonInt         : Int?                     = null,
  @SerializedName("episodes"          ) var episodes          : String?                  = null,
  @SerializedName("duration"          ) var duration          : Int?                     = null,
  @SerializedName("chapters"          ) var chapters          : String?                  = null,
  @SerializedName("volumes"           ) var volumes           : String?                  = null,
  @SerializedName("countryOfOrigin"   ) var countryOfOrigin   : String?                  = null,
  @SerializedName("source"            ) var source            : String?                  = null,
  @SerializedName("hashtag"           ) var hashtag           : String?                  = null,
//  @SerializedName("trailer"           ) var trailer           : String?                  = null,
  @SerializedName("coverImage"        ) var coverImage        : CoverImage?              = CoverImage(),
  @SerializedName("bannerImage"       ) var bannerImage       : String?                  = null,
  @SerializedName("genres"            ) var genres            : ArrayList<String>        = arrayListOf(),
  @SerializedName("synonyms"          ) var synonyms          : ArrayList<String>        = arrayListOf(),
  @SerializedName("averageScore"      ) var averageScore      : Int?                     = null,
  @SerializedName("meanScore"         ) var meanScore         : Int?                     = null,
  @SerializedName("popularity"        ) var popularity        : Int?                     = null,
  @SerializedName("favourites"        ) var favourites        : Int?                     = null,
  @SerializedName("tags"              ) var tags              : ArrayList<Tags>          = arrayListOf(),
  @SerializedName("relations"         ) var relations         : Relations?               = Relations(),
  @SerializedName("characters"        ) var characters        : Characters?              = Characters(),
  @SerializedName("studios"           ) var studios           : Studios?                 = Studios(),
  @SerializedName("isFavourite"       ) var isFavourite       : Boolean?                 = null,
  @SerializedName("isAdult"           ) var isAdult           : Boolean?                 = null,
//  @SerializedName("nextAiringEpisode" ) var nextAiringEpisode : NextAiringEpisode?       = NextAiringEpisode(),
//  @SerializedName("airingSchedule"    ) var airingSchedule    : AiringSchedule?          = AiringSchedule(),
  @SerializedName("externalLinks"     ) var externalLinks     : ArrayList<ExternalLinks> = arrayListOf(),
  @SerializedName("recommendations"   ) var recommendations   : Recommendations?         = Recommendations(),
  @SerializedName("siteUrl"           ) var siteUrl           : String?                  = null

)