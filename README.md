### Art of TNT, a Minecraft Mod
#### tnt frame addition customization:
put additions json in data/**[namespace]**/tnt_frame_additions, use artoftnt namespace and a same file name to overwrite.  
**demo:**  
`data/artoftnt/gunpowder.json`  
_//the texture or the model file(if specialRenderer is true and is not built in) should be put in_  
_//assets/[namespace]/textures/tnt_frame_additions or_  
_//assets/[namespace]/models/tnt_frame_additions with a same name_  
`{`  
`"type": "range",` _//the addition type, see @AdditionType_  
`"item": "minecraft:gunpowder",` _//the registry name of the item to add, shouldn't duplicate_  
`"increment": 1.0,` _//the increment to the addition type, may be negative when type is instability_  
`"minTier": 1,` _//the min level of the tnt frame to put, 0-3_  
`"maxCount": 8,` _//max count of the addition, will also be restricted by the max count of type and slot_  
`"weight": 0.0,` _//weight added to the tnt frame_  
`"instability": 0.0,` _//instability added to the tnt frame, should be 0 when type is instability_  
`"specialRenderer": false` _//whether to use special renderer, some have built in models, or you can add models_  
}`
