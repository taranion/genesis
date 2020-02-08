/**
 * 
 */
package org.prelle.genesis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.rpgframework.ConfigChangeListener;
import de.rpgframework.ConfigContainer;
import de.rpgframework.ConfigNode;
import de.rpgframework.ConfigOption;
import de.rpgframework.ResourceI18N;
import de.rpgframework.character.Attachment;
import de.rpgframework.character.CharacterHandle;
import de.rpgframework.character.CharacterHandle.Format;
import de.rpgframework.character.CharacterHandle.Type;
import de.rpgframework.core.RoleplayingSystem;

/**
 * @author Stefan
 *
 */
@SuppressWarnings("exports")
public class ExternalTools implements ConfigChangeListener {

	private final static Logger logger = LogManager.getLogger("genesis");

	public final static ResourceBundle RES = ResourceBundle.getBundle(ExternalTools.class.getName());

	private ConfigContainer cfgExternal;

	private Map<RoleplayingSystem, ConfigContainer> ruleMap;
	
	//-------------------------------------------------------------------
	public void prepareConfigNodes(ConfigContainer cfgGenesis) {
		cfgExternal = cfgGenesis.createContainer("external");
		cfgExternal.addListener(this);

		// Create subnodes for each RPG
		ruleMap = new HashMap<>();
		for (RoleplayingSystem rules : RoleplayingSystem.values()) {
			if (rules==RoleplayingSystem.ALL)
				continue;
			ConfigContainer perRules = cfgExternal.createContainer(rules.name().toLowerCase());
			perRules.setName(rules.getName());
			ruleMap.put(rules, perRules);
			ConfigOption<?> opt = perRules.createOption("path", ConfigOption.Type.FILE, null);
			opt.setName(ResourceI18N.get(RES, "label.path_to_external"));
		}
	}

	//--------------------------------------------------------------------
	@SuppressWarnings("unchecked")
	public void setTool(RoleplayingSystem rules, Path path) {
		ConfigContainer perRules = ruleMap.get(rules);		
		ConfigOption<String> cfgPath = (ConfigOption<String>) perRules.getOption("path");
		cfgPath.set(path.toAbsolutePath().toString());
	}

	//--------------------------------------------------------------------
	@SuppressWarnings("unchecked")
	public void unsetTool(RoleplayingSystem rules) {
		ConfigContainer perRules = ruleMap.get(rules);		
		ConfigOption<String> cfgPath = (ConfigOption<String>) perRules.getOption("path");
		cfgPath.set(null);
	}

	//--------------------------------------------------------------------
	@SuppressWarnings("unchecked")
	public Path getToolPath(RoleplayingSystem rules) {
		ConfigContainer perRules = (ConfigContainer) cfgExternal.getChild(rules.name().toLowerCase());
		ConfigOption<String> cfgPath = (ConfigOption<String>) perRules.getChild("path");
		if (cfgPath==null || cfgPath.getValue()==null)
			return null;
		return FileSystems.getDefault().getPath(cfgPath.getStringValue());
	}

	//--------------------------------------------------------------------
	@SuppressWarnings("unchecked")
	public Map<RoleplayingSystem, Path> getConfiguredPathes() {
		logger.debug("Get pathes of "+cfgExternal);
		Map<RoleplayingSystem, Path> ret = new HashMap<>();
		if (cfgExternal!=null) { 
			for (ConfigNode node : cfgExternal) {
				logger.debug("..."+node);
				if (node instanceof ConfigContainer) {
					try {
						ConfigContainer perRules = (ConfigContainer)node;
						RoleplayingSystem rules = RoleplayingSystem.valueOf(perRules.getLocalId().toUpperCase());
						ConfigOption<String> cfgPath = (ConfigOption<String>)perRules.getOption("path");
						if (cfgPath.getValue()==null)
							continue;
						Path path = FileSystems.getDefault().getPath(cfgPath.getValue()+"");
						logger.debug("...Found "+rules+" = "+path);
						if (!Files.exists(path))
							logger.warn("Referenced external application for "+rules+" does not exist: "+path);
						ret.put(rules, path);
					} catch (Exception e) {
						logger.error("Failed obtaining data from "+node,e);
					}
				}
			}
		} else {
			logger.error("No config node for external tools");
		}
		return ret;
	}

	//--------------------------------------------------------------------
	public boolean canOpen(CharacterHandle handle) {
		logger.debug("canOpen("+handle+")");
		Attachment attach = handle.getFirstAttachment(Type.CHARACTER, Format.RULESPECIFIC_EXTERNAL);
		if (attach==null)
			attach = handle.getFirstAttachment(Type.CHARACTER, Format.RULESPECIFIC);
		RoleplayingSystem rules = handle.getRuleIdentifier();
		Path path = getToolPath(rules);
		logger.debug("... path would be "+path);
		logger.debug("... attachment is "+attach);
		return path!=null && attach!=null;
	}

	//--------------------------------------------------------------------
	public void open(CharacterHandle handle) throws IOException {
		logger.info("Open handle "+handle);

		Attachment attach = handle.getFirstAttachment(Type.CHARACTER, Format.RULESPECIFIC_EXTERNAL);
		if (attach==null)
			attach = handle.getFirstAttachment(Type.CHARACTER, Format.RULESPECIFIC);
		Path realPath = handle.getPath().resolve(attach.getFilename());
		logger.debug("Open file "+realPath);
		//		try {
		Path toolPath = getToolPath(handle.getRuleIdentifier());
		if (toolPath==null) {
			throw new IOException(ResourceI18N.format(RES, "error.externaltools.unconfigured", handle.getRuleIdentifier().getName()));
		}
		
		logger.info("Call "+toolPath+" "+realPath);
		Process pro = Runtime.getRuntime().exec(new String[]{toolPath.toString(), realPath.toString()});
		logger.debug("Process = "+pro);
		StringWriter out = new StringWriter();
		BufferedReader stdErr = new BufferedReader(new InputStreamReader(pro.getErrorStream()));
		BufferedReader stdOut = new BufferedReader(new InputStreamReader(pro.getInputStream()));
		boolean hasError = false;
		while (true) {
			String lineErr = stdErr.readLine();
			String lineOut = stdOut.readLine();
			if (lineOut!=null) {
				out.append(lineOut+"\r\n");
				logger.warn("OUT: "+lineOut);
			}
			if (lineErr!=null) {
				logger.warn("ERR: "+lineErr);
				out.append(lineErr+"\r\n");
				hasError = true;
			}
			if (lineErr==null && lineOut==null)
				break;
		}
		//			}
		logger.info("Process = "+pro.isAlive());
		if (!pro.isAlive() && hasError)
			throw new IOException("Failed calling: "+toolPath+" "+realPath+"\n\n"+out);
		//		} catch (IOException e) {
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		}
	}

	//-------------------------------------------------------------------
	/**
	 * @see de.rpgframework.ConfigChangeListener#configChanged(de.rpgframework.ConfigContainer, java.util.Collection)
	 */
	@Override
	public void configChanged(ConfigContainer source, Collection<ConfigOption<?>> options) {
		// TODO Auto-generated method stub
		logger.info("Config changed");
	}
}
