import os 
from openai import OpenAI


async def main():    
    client = OpenAI(
        # 若没有配置环境变量，请用百炼API Key将下行替换为：api_key="sk-xxx",
        api_key="sk-7438e0554ba94b6c9b96f2e619c2f5dc",
        base_url="https://dashscope.aliyuncs.com/compatible-mode/v1",
    )

    completion = client.chat.completions.create(
        # 模型列表：https://help.aliyun.com/zh/model-studio/getting-started/models
        model="qwen-plus",
        messages=[
            {"role": "system", "content": "将文本分类为中性、负面或正面。文本：我认为这次假期还可以。"},
            {"role": "user", "content": "情感"},
        ],
        # Qwen3模型通过enable_thinking参数控制思考过程（开源版默认True，商业版默认False）
        # 使用Qwen3开源版模型时，若未启用流式输出，请将下行取消注释，否则会报错
        # extra_body={"enable_thinking": False},
    )
    print(completion.model_dump_json())


if __name__ == "__main__":
    import asyncio
    asyncio.run(main())

  
"""
{
    "id": "chatcmpl-28f88c87-32db-9e6b-b899-2246316962f6",
    "choices": [
        {
            "finish_reason": "stop",
            "index": 0,
            "logprobs": null,
            "message": {
                "content": "该文本的情感分类为：**中性**。\n\n**理由**：  \n“我认为这次假期还可以。” 中的 “还可以” 表达的是一种中立、平淡的态度，没有明显的积极或消极情绪。",
                "refusal": null,
                "role": "assistant",
                "annotations": null,
                "audio": null,
                "function_call": null,
                "tool_calls": null
            }
        }
    ],
    "created": 1753520065,
    "model": "qwen-plus",
    "object": "chat.completion",
    "service_tier": null,
    "system_fingerprint": null,
    "usage": {
        "completion_tokens": 45,
        "prompt_tokens": 36,
        "total_tokens": 81,
        "completion_tokens_details": null,
        "prompt_tokens_details": {
            "audio_tokens": null,
            "cached_tokens": 0
        }
    }
}
"""